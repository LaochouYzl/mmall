package com.mmall.service.impl;


import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.utils.MD5Util;


@Service("iUserService")
public class UserServiceImpl implements IUserService{
	
	@Autowired
	private UserMapper userMapper;

	/**
	 * 登录服务
	 */
	@Override
	public ServerResponse<User> login(String username, String password) {
		// 判断用户名是否存在
		int resultCount = userMapper.checkUsername(username);
		if(resultCount == 0) {
			return ServerResponse.createByErrorMessage("用户名不存在");
		}
		//todo 密码登录MD5加密
		String md5Password = MD5Util.MD5EncodeUtf8(password);
		// 检验用户
		User user = userMapper.selectLogin(username, md5Password);
		if(user == null) {
			return ServerResponse.createByErrorMessage("密码错误");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess("登录成功", user);
	}
	
	/**
	 * 注册服务
	 */
	@Override
	public ServerResponse<String> register(User user){
		System.out.println(user);
		ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
		if(!validResponse.isSuccess()) {
			return validResponse;
		}
		validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
		if(!validResponse.isSuccess()) {
			return validResponse;
		}
		user.setRole(Const.Role.ROLE_CUSTOMER);
		// MD5加密
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		int resultCount = userMapper.insert(user);
		if(resultCount == 0) {
			// 表示可能服务器出现问题或者db错误
			return ServerResponse.createByErrorMessage("注册失败");
		}
		return ServerResponse.createBySuccessMessage("注册成功");
	}
	
	/**
	 * 校验用户名或邮箱服务
	 */
	@Override
	public ServerResponse<String> checkValid(String str, String type){
		/**
		 * 在这里说一下StringUtils包里面的问题, isEmpty和isBlank函数的区别, 在isEmpty中 " " 也是true, 但是在isblank中 " " 是false
		 */
		if(!StringUtils.isBlank(type)) {
			// 开始校验
			if(Const.USERNAME.equals(type)) {
				int resultCount = userMapper.checkUsername(str);
				if(resultCount > 0) {
					return ServerResponse.createByErrorMessage("用户已存在");
				}
			}else if(Const.EMAIL.equals(type)) {
				int resultCount = userMapper.checkEmail(str);
				if(resultCount > 0) {
					return ServerResponse.createByErrorMessage("email已存在");
				}
			}
		}else {
			return ServerResponse.createByErrorMessage("参数错误");
		}
		return ServerResponse.createBySuccessMessage("校验通过");
	}
	
	/**
	 * 查询问题
	 */
	@Override
	public ServerResponse<String> selectQuestion(String username) {
		ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
		if(validResponse.isSuccess()) {
			// 用户不存在
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String question = userMapper.selectQuestionByUsername(username);
		if(StringUtils.isNotBlank(question)) {
			return ServerResponse.createBySuccess(question);
		}
		return ServerResponse.createByErrorMessage("找回密码的问题是空的");
	}
	
	/**
	 *  校对问题和答案已经是否属于用户
	 */
	@Override
	public ServerResponse<String> checkAnswer(String username, String question, String answer){
		int resultCount = userMapper.checkAnswer(username, question, answer);
		if(resultCount > 0) {
			// 说明问题以及问题答案是这个用户的, 并且是正确的
			String forgetToken = UUID.randomUUID().toString();
			TokenCache.setKey(TokenCache.TOEKN_PREFIX+username, forgetToken);
			return ServerResponse.createBySuccess(forgetToken);
		}
		return ServerResponse.createByErrorMessage("问题的答案错误");
	}
	
	/**
	 * 	在未登录的情况下, 重置密码
	 */
	@Override
	public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
		if(StringUtils.isBlank(forgetToken)) {
			return ServerResponse.createByErrorMessage("参数错误, token需要传递");
		}
		ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
		if(validResponse.isSuccess()) {
			// 用户不存在
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String token = TokenCache.getKey(TokenCache.TOEKN_PREFIX+username);
		if(StringUtils.isBlank(token)) {
			return ServerResponse.createByErrorMessage("token无效或者过期");
		}
		if(StringUtils.equals(forgetToken, token)) {
			String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
			int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
			if(rowCount > 0) {
				return ServerResponse.createBySuccessMessage("修改密码成功");
			}
		}else {
			return ServerResponse.createByErrorMessage("token错误, 请重新获取重置密码的token");
		}
		return ServerResponse.createByErrorMessage("修改密码失败");
	}
	
	/**
	 * 	在登录的状态下, 进行重置密码
	 */
	@Override
	public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
		// 防止横向越权, 需要校验这个用户的旧密码, 一定要指定是这个用户,因为我们会查询一个count(1)
		// 如果不指定id, 那么结果就是true啦, count> 0
		int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
		if(resultCount == 0) {
			return ServerResponse.createByErrorMessage("旧密码错误");
		}
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int updateCount = userMapper.updateByPrimaryKeySelective(user);
		if(updateCount > 0) {
			return ServerResponse.createBySuccessMessage("密码更新成功");
		}
		return ServerResponse.createByErrorMessage("密码更新失败");
	}
	
	/**
	 * 	更改个人信息
	 */
	public ServerResponse<User> updateInfomation(User user){
		// username 是不能被更新的
		// email需要进行校验, 校验新的email是否已经存在, 并且存在的email如果相同的话, 不能是我们当前这个用户的
		int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
		if(resultCount > 0) {
			return ServerResponse.createByErrorMessage("email已经存在,请更换email, 再尝试更新");
		}
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());
		int updateCount = userMapper.updateByPrimaryKeySelective(user);
		if(updateCount > 0) {
			return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
		}
		return ServerResponse.createByErrorMessage("更新个人信息失败");
	}
	
	/**
	 * 	登录状态下, 获取个人信息
	 */
	public ServerResponse<User> getInfomation(Integer userId){
		User user = userMapper.selectByPrimaryKey(userId);
		if(user == null) {
			return ServerResponse.createByErrorMessage("找不到当前用户");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess(user);
	}
	
	// backend
	/**
	 * 	校验用户是否是管理员
	 * @param user
	 * @return
	 */
	public ServerResponse<?> checkAdminRole(User user) {
		if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
			return ServerResponse.createBySuccess();
		}
		return ServerResponse.createByError();
	}
	
	
	
	

}
