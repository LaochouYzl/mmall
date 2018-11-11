package com.mmall.dao;

import org.apache.ibatis.annotations.Param;

import com.mmall.pojo.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    //检查用户名是否存在
    int checkUsername(String username);
    
    // 检查邮箱是否已注册
    int checkEmail(String email);
    
    // 在传递多个参数的时候需要@Param参数
    User selectLogin(@Param("username")String username, @Param("password")String password);
    
    // 查找问题
    String selectQuestionByUsername(String username);
    
    // 校验问题和答案
    int checkAnswer(@Param("username")String username, @Param("question")String question, @Param("answer")String answer);
    
    // 未登录情况下更新密码
    int updatePasswordByUsername(@Param("username")String username, @Param("passwordNew")String passwordNew);
    
    // 校验密码
    int checkPassword(@Param("password")String password, @Param("userId")Integer userId);
    
    // 校验邮箱
    int checkEmailByUserId(@Param("email")String email, @Param(value="userId")Integer userId);
    
    
}