package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AIProductRequestDTO;
import com.mall.dto.AIProductResponseDTO;
import com.mall.entity.Product;
import com.mall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取商品列表
     */
    @GetMapping
    public Result<List<Product>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> productList = productService.getProductList(page, size);
        return Result.success(productList);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }
    
    /**
     * 商品搜索
     */
    @GetMapping("/search")
    public Result<List<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> products = productService.searchProducts(keyword, page, size);
        return Result.success(products);
    }
    
    /**
     * 根据分类ID获取商品列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> products = productService.getProductsByCategory(categoryId, page, size);
        return Result.success(products);
    }
    
    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return Result.success(products);
    }
    
    /**
     * 获取最新商品
     */
    @GetMapping("/new")
    public Result<List<Product>> getNewProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getNewProducts(limit);
        return Result.success(products);
    }
    
    /**
     * AI生成商品文案
     */
    @PostMapping("/generate-content")
    public Result<AIProductResponseDTO> generateProductContent(@RequestBody AIProductRequestDTO request) {
        try {
            // 调用服务层生成商品文案
            AIProductResponseDTO result = productService.generateProductContent(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成商品文案失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 添加商品
     */
    @PostMapping
    public Result<Boolean> addProduct(@RequestBody Product product) {
        boolean result = productService.addProduct(product);
        return Result.success(result);
    }
    
    /**
     * 更新商品
     */
    @PutMapping
    public Result<Boolean> updateProduct(@RequestBody Product product) {
        boolean result = productService.updateProduct(product);
        return Result.success(result);
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        boolean result = productService.deleteProduct(id);
        return Result.success(result);
    }
    
    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = productService.updateProductStatus(id, status);
        return Result.success(result);
    }
    
    /**
     * 管理员获取商品列表（包含所有状态）
     */
    @GetMapping("/admin/list")
    public Result<List<Product>> getAdminProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<Product> productList = productService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreateTime)
        );
        return Result.success(productList);
    }
}
