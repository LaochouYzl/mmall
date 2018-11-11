package com.mmall.common;

/**
 * 	常量类
 * @author Administrator
 *
 */
public class Const {
	
	public static final String CURRENT_USER = "currentUser";
	
	public static final String EMAIL = "email";
	
	public static final String USERNAME = "username";
	
	// 内部接口, 可以起到分组的作用, 如果使用枚举的话, 就太繁重了
	public interface Role {
		int ROLE_CUSTOMER = 0; //普通用户
		int ROLE_ADMIN = 1; // 管理员
	}

}
