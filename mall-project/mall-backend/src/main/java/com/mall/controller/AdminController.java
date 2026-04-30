package com.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.Result;
import com.mall.dto.LoginRequest;
import com.mall.dto.LoginResponse;
import com.mall.entity.Product;
import com.mall.entity.User;
import com.mall.service.ProductService;
import com.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    // ========== 商品管理 ==========
    
    /**
     * 获取所有商品
     */
    @GetMapping("/products")
    public Result<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> productList = productService.getProductList(page, size);
        return Result.success(productList);
    }
    
    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<?> addProduct(@RequestBody Product product) {
        // 这里可以添加商品添加逻辑
        return Result.success("商品添加成功");
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // 这里可以添加商品更新逻辑
        return Result.success("商品更新成功");
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        // 这里可以添加商品删除逻辑
        return Result.success("商品删除成功");
    }
    
    // ========== 用户管理 ==========
    
    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public Result<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {
        List<User> userList = userService.getUserList(page, size);
        // 可以根据keyword、status、role进行过滤（这里暂不实现复杂过滤）
        return Result.success(userList);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        user.setId(id);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        log.info("更新用户信息成功: id={}", id);
        return Result.success("用户信息更新成功");
    }
    
    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = userService.updateUserStatus(id, status);
        if (result) {
            return Result.success("用户状态更新成功");
        } else {
            return Result.error("用户状态更新失败");
        }
    }
    
    /**
     * 设置用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        boolean result = userService.updateUserRole(id, role);
        if (result) {
            return Result.success("用户角色更新成功");
        } else {
            return Result.error("用户角色更新失败");
        }
    }

    /**
     * 管理员登录
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<LoginResponse> adminLogin(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.adminLogin(request);
        return Result.success(response);
    }
}
