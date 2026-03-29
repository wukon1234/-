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
        // 1. 查询候选商品列表（先按状态、可选过滤条件筛一遍）
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
        
        // 2. 计算简单匹配得分，按得分排序后截断
        List<ProductScore> scored = products.stream()
            .map(p -> new ProductScore(p, scoreProduct(p, query)))
            .filter(ps -> ps.score > 0) // 过滤掉完全不匹配的
            .sorted((a, b) -> Float.compare(b.score, a.score))
            .limit(topK)
            .collect(Collectors.toList());
        
        // 3. 转换为Map格式
        return scored.stream()
            .map(ps -> toMap(ps.product))
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
     * 简单打分：名称 > 描述 > 规格，支持中英文、模糊匹配与多关键词
     */
    private float scoreProduct(Product product, String query) {
        if (query == null || query.trim().isEmpty()) {
            // 无查询时返回基础分，让“热门商品”等场景可以直接走召回逻辑
            return 1.0f;
        }

        String q = query.trim().toLowerCase();
        // 按空格/中文逗号/英文逗号拆分成若干关键词
        String[] keywords = q.split("[,，\\s]+");

        String name = product.getName() == null ? "" : product.getName().toLowerCase();
        String desc = product.getDescription() == null ? "" : product.getDescription().toLowerCase();
        String specs = product.getSpecs() == null ? "" : product.getSpecs().toLowerCase();

        float score = 0f;
        for (String kw : keywords) {
            if (kw.isEmpty()) continue;

            // 完全包含给予较高权重，出现在名称比分数更高
            if (name.contains(kw)) {
                score += 5f;
            } else if (desc.contains(kw)) {
                score += 3f;
            } else if (specs.contains(kw)) {
                score += 2f;
            }
        }

        // 价格区间类问法（例如“便宜”“高端”等）可以根据价格做一个粗略奖励/惩罚（可选）
        if (score > 0 && product.getPrice() != null) {
            float price = product.getPrice().floatValue();
            String raw = query;
            if (raw.contains("便宜") || raw.contains("入门") || raw.toLowerCase().contains("cheap")) {
                // 价格越低，额外加分越多（简单反比）
                score += Math.max(0f, 10000f / (price + 1f)) * 0.01f;
            } else if (raw.contains("高端") || raw.contains("旗舰") || raw.toLowerCase().contains("expensive")) {
                // 价格越高，额外加分越多
                score += Math.min(price, 20000f) * 0.001f;
            }
        }

        return score;
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

    /**
     * 内部打分结构体
     */
    private static class ProductScore {
        private final Product product;
        private final float score;

        private ProductScore(Product product, float score) {
            this.product = product;
            this.score = score;
        }
    }
}

