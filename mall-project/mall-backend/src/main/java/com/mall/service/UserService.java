package com.mall.service;

import com.mall.dto.LoginRequest;
import com.mall.dto.LoginResponse;
import com.mall.dto.RegisterRequest;
import com.mall.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户注册
     */
    void register(RegisterRequest request);
    
    /**
     * 根据ID获取用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 根据用户名获取用户信息
     */
    User getUserByUsername(String username);
    
    /**
     * 修改用户密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 管理员登录
     */
    LoginResponse adminLogin(LoginRequest request);
}

