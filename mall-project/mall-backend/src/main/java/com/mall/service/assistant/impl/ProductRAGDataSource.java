package com.mall.service.assistant.impl;

import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import com.mall.service.assistant.DataSource;
import com.mall.service.assistant.RAGDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品数据源实现
 */
@Component
public class ProductRAGDataSource implements RAGDataSource {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public DataSource getType() {
        return DataSource.PRODUCT;
    }
    
    @Override
    public List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters) {
        // 查询商品列表
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1); // 只查询上架商品
        
        // 应用过滤条件
        if (filters != null) {
            if (filters.containsKey("categoryId")) {
                queryWrapper.eq(Product::getCategoryId, filters.get("categoryId"));
            }
            if (filters.containsKey("minPrice")) {
                queryWrapper.ge(Product::getPrice, filters.get("minPrice"));
            }
            if (filters.containsKey("maxPrice")) {
                queryWrapper.le(Product::getPrice, filters.get("maxPrice"));
            }
        }
        
        List<Product> products = productMapper.selectList(queryWrapper);
        
        // 简单的关键词匹配（后续可以用向量搜索替换）
        List<Product> matchedProducts = products.stream()
            .filter(product -> matchesQuery(product, query))
            .limit(topK)
            .collect(Collectors.toList());
        
        // 转换为Map格式
        return matchedProducts.stream()
            .map(this::toMap)
            .collect(Collectors.toList());
    }
    
    @Override
    public String toText(Map<String, Object> data) {
        // 构建用于向量化的文本
        StringBuilder text = new StringBuilder();
        text.append(data.get("name")).append(" ");
        if (data.containsKey("description") && data.get("description") != null) {
            text.append(data.get("description")).append(" ");
        }
        if (data.containsKey("categoryName") && data.get("categoryName") != null) {
            text.append("分类：").append(data.get("categoryName")).append(" ");
        }
        if (data.containsKey("specs") && data.get("specs") != null) {
            text.append("规格：").append(data.get("specs"));
        }
        return text.toString().trim();
    }
    
    @Override
    public String getDescription() {
        return "商品数据源，包含商品名称、描述、价格、规格等信息";
    }
    
    /**
     * 判断商品是否匹配查询
     */
    private boolean matchesQuery(Product product, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        return (product.getName() != null && product.getName().toLowerCase().contains(lowerQuery)) ||
               (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery));
    }
    
    /**
     * 将Product转换为Map
     */
    private Map<String, Object> toMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("type", "product");
        map.put("name", product.getName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice());
        map.put("categoryId", product.getCategoryId());
        map.put("stock", product.getStock());
        map.put("imageUrl", product.getImageUrl());
        map.put("specs", product.getSpecs());
        return map;
    }
}

