package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.AIProductRequestDTO;
import com.mall.dto.AIProductResponseDTO;
import com.mall.entity.Product;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {
    
    /**
     * 获取商品列表
     */
    List<Product> getProductList(Integer page, Integer size);
    
    /**
     * 获取商品详情
     */
    Product getProductById(Long id);
    
    /**
     * 商品搜索
     */
    List<Product> searchProducts(String keyword, Integer page, Integer size);
    
    /**
     * 根据分类ID获取商品列表
     */
    List<Product> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    /**
     * 获取热门商品
     */
    List<Product> getHotProducts(Integer limit);
    
    /**
     * 获取最新商品
     */
    List<Product> getNewProducts(Integer limit);
    
    /**
     * AI生成商品文案
     */
    AIProductResponseDTO generateProductContent(AIProductRequestDTO request);
    
    /**
     * 添加商品
     */
    boolean addProduct(Product product);
    
    /**
     * 更新商品
     */
    boolean updateProduct(Product product);
    
    /**
     * 删除商品
     */
    boolean deleteProduct(Long id);
    
    /**
     * 更新商品状态
     */
    boolean updateProductStatus(Long id, Integer status);
}
