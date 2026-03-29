package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.LoginRequest;
import com.mall.dto.LoginResponse;
import com.mall.dto.RegisterRequest;
import com.mall.dto.UpdatePasswordRequest;
import com.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest request) {
        log.info("用户注册: {}", request.getUsername());
        userService.register(request);
        return Result.success("注册成功");
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getUserById(userId));
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Result<?> updatePassword(@RequestAttribute("userId") Long userId, 
                                    @Validated @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }
    
    /**
     * 管理员登录
     */

}

