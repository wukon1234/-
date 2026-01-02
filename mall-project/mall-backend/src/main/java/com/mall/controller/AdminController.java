package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.Product;
import com.mall.entity.User;
import com.mall.service.ProductService;
import com.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "10") Integer size) {
        // 这里可以添加获取所有用户的逻辑
        return Result.success(null);
    }
    
    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 这里可以添加更新用户状态的逻辑
        return Result.success("用户状态更新成功");
    }
    
    /**
     * 设置用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        // 这里可以添加更新用户角色的逻辑
        return Result.success("用户角色更新成功");
    }
}
