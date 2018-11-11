package com.mmall.controller.portal;


import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;

@Controller
@RequestMapping("/user/")
public class UserController {
	
	
	@Resource(name="iUserService")
	private IUserService iUserService;
	
	/**
	 *	 测试
	 */
	@RequestMapping(value = "test")
	@ResponseBody
	public String test() {
		return "success";
	}
	
	
	/**
	 * 用户登录
	 * @param username
	 * @param password
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "login.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> login(String username, String password, HttpSession session) {
		System.out.println(username+":"+password);
		ServerResponse<User> response = iUserService.login(username, password);
		if(response.isSuccess()) {
			session.setAttribute(Const.CURRENT_USER, response.getData());
		}
		return response;
	}
	
	/**
	 * 退出登录, 我们只需要将当前用户的session信息清除掉即可
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "logout.do", method = RequestMethod.GET)
	@ResponseBody
	public ServerResponse<String> logout(HttpSession session){
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccess();
	}
	
	
	/**
	 * 注册
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "register.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user){
		return iUserService.register(user);
	}
	
	
	/**
	 * 校验用户用户名或邮件
	 * @param str
	 * @param type
	 * @return
	 */
	@RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkValid(String str, String type){
		return iUserService.checkValid(str, type);
	}
	
	
	/**
	 * 获取用户信息
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session){
		User user = (User)session.getAttribute(Const.CURRENT_USER);
		if(user != null) {
			return ServerResponse.createBySuccess(user);
		}
		return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
	}
	
	/**
	 * 问题获取
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetGetQuestion(String username){
		return iUserService.selectQuestion(username);
	}
	
	
	/**
	 * 	检查答案
	 * @param username
	 * @param question
	 * @param answer
	 * @return
	 */
	@RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> frogetCheckAnswer(String username, String question, String answer){
		return iUserService.checkAnswer(username, question, answer);
	}
	
	
	/**
	 * 	忘记密码的情况下重置密码
	 * @param username
	 * @param passwordNew
	 * @param forgetToken
	 * @return
	 */
	@RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
		return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
	}
	
	/**
	 * 	登录状态下的重置密码
	 * @return
	 */
	@RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
		User user = (User) session.getAttribute(Const.CURRENT_USER);
		if(user == null) {
			return ServerResponse.createByErrorMessage("用户未登录");
		}
		return iUserService.resetPassword(passwordOld, passwordNew, user);
	}
	
	/**
	 * 
	 * @param session
	 * @param user 需要更新的user信息, 一般都是邮箱, 电话号码之类的 
	 * @return
	 */
	@RequestMapping(value = "update_infomation.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> updateInfomation(HttpSession session, User user){
		// 判断登录
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if(currentUser == null) {
			return ServerResponse.createByErrorMessage("用户未登录");
		}
		user.setId(currentUser.getId());
		user.setUsername(currentUser.getUsername());
		ServerResponse<User> response = iUserService.updateInfomation(user);
		if(response.isSuccess()) {
			session.setAttribute(Const.CURRENT_USER, response.getData());
		}
		return response;
	}
	
	
	/**
	 * 获取个人信息
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "get_infomation.do", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getInfomation(HttpSession session){
		User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
		if(currentUser == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录需要强制登录");
		}
		return iUserService.getInfomation(currentUser.getId());
	}
	
	
	
	
	
	
	
	
}
