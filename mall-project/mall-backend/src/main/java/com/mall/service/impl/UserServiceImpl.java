package com.mall.service.impl;

import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.dto.LoginRequest;
import com.mall.dto.LoginResponse;
import com.mall.dto.RegisterRequest;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import com.mall.service.UserService;
import com.mall.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
        );
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(400, "用户已被禁用");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        
        return response;
    }
    
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
        );
        
        if (existingUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用
        user.setRole(1); // 默认普通用户
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", request.getUsername());
    }
    
    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    @Override
    public LoginResponse adminLogin(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
        );
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(400, "用户已被禁用");
        }
        
        // 检查是否为管理员
        if (user.getRole() == null || user.getRole() != 2) {
            throw new BusinessException(403, "权限不足，需要管理员权限");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        
        return response;
    }
}

