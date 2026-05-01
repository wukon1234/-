package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AIProductRequestDTO;
import com.mall.dto.AIProductResponseDTO;
import com.mall.entity.Product;
import com.mall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;
    
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
    public Result<Map<String, Object>> getAdminProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .orderByDesc(Product::getCreateTime);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Product::getName, kw).or().like(Product::getDescription, kw));
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> resultPage = productService.page(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        return Result.success(result);
    }

    /**
     * 管理员获取商品详情（包含下架商品）
     */
    @GetMapping("/admin/{id}")
    public Result<Product> getAdminProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String origin = file.getOriginalFilename();
        String suffix = ".jpg";
        if (origin != null && origin.lastIndexOf('.') > -1) {
            suffix = origin.substring(origin.lastIndexOf('.')).toLowerCase();
        }
        if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix) && !".webp".equals(suffix)) {
            return Result.error("仅支持 jpg/jpeg/png/webp 图片");
        }
        File dir = new File(uploadPath, "product");
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.error("创建上传目录失败");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        File target = new File(dir, fileName);
        try {
            file.transferTo(target);
            Map<String, String> result = new HashMap<>();
            result.put("url", "/api/products/image/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            result.put("fileName", fileName);
            return Result.success(result);
        } catch (IOException e) {
            log.error("上传商品图片失败", e);
            return Result.error("上传商品图片失败");
        }
    }

    /**
     * 读取商品图片
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String fileName) {
        File file = new File(new File(uploadPath, "product"), fileName);
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String lower = fileName.toLowerCase();
        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
