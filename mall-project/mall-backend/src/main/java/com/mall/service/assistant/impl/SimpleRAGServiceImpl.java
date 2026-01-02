package com.mall.service.assistant.impl;

import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import com.mall.service.assistant.DataSource;
import com.mall.service.assistant.EmbeddingService;
import com.mall.service.assistant.RAGDataSource;
import com.mall.service.assistant.RAGService;
import com.mall.service.assistant.vector.VectorStore;
import com.mall.config.RAGConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 简单RAG服务实现（暂时使用关键词匹配，后续可替换为向量检索）
 * 支持多数据源
 */
@Slf4j
@Service
public class SimpleRAGServiceImpl implements RAGService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired(required = false)
    private List<RAGDataSource> dataSources;

    @Autowired(required = false)
    private EmbeddingService embeddingService;

    @Autowired(required = false)
    private VectorStore vectorStore;

    @Autowired(required = false)
    private RAGConfig ragConfig;
    
    // 数据源注册表
    private final Map<DataSource, RAGDataSource> dataSourceMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // 自动注册所有数据源
        if (dataSources != null) {
            for (RAGDataSource dataSource : dataSources) {
                registerDataSource(dataSource);
            }
        }
        log.info("RAG服务初始化完成，已注册数据源: {}", dataSourceMap.keySet());

        // 初始化向量库（可选）
        if (isVectorEnabled()) {
            try {
                if (vectorStore != null) {
                    vectorStore.init();
                }
                if (Boolean.TRUE.equals(ragConfig.getVectorAutoSync())) {
                    syncAllProductsToVectorStore();
                }
            } catch (Exception e) {
                log.warn("向量检索初始化失败，将降级为关键词检索: {}", e.getMessage());
            }
        }
    }
    
    @Override
    public List<Product> searchProducts(String query, int topK) {
        log.info("RAG检索商品，查询: {}, topK: {}", query, topK);

        // 优先使用向量检索（配置开启且依赖就绪）
        List<Map<String, Object>> results;
        if (shouldUseVectorSearch()) {
            results = vectorSearchProducts(query, topK);
            if (results.isEmpty()) {
                // 兜底：向量检索为空则回退关键词检索
                results = search(query, DataSource.PRODUCT, topK, null);
            }
        } else {
            // 使用商品数据源进行搜索（关键词匹配）
            results = search(query, DataSource.PRODUCT, topK, null);
        }
        
        // 转换为Product对象
        return results.stream()
            .map(this::mapToProduct)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> search(String query, DataSource dataSource, int topK, Map<String, Object> filters) {
        log.info("RAG检索，数据源: {}, 查询: {}, topK: {}", dataSource, query, topK);
        
        RAGDataSource ragDataSource = dataSourceMap.get(dataSource);
        if (ragDataSource == null) {
            log.warn("数据源未注册: {}", dataSource);
            return new ArrayList<>();
        }
        
        return ragDataSource.search(query, topK, filters);
    }
    
    @Override
    public Map<DataSource, List<Map<String, Object>>> searchMulti(String query, List<DataSource> dataSources, int topK, Map<String, Object> filters) {
        log.info("RAG多数据源检索，查询: {}, 数据源: {}, topK: {}", query, dataSources, topK);
        
        Map<DataSource, List<Map<String, Object>>> results = new HashMap<>();
        for (DataSource ds : dataSources) {
            results.put(ds, search(query, ds, topK, filters));
        }
        return results;
    }
    
    @Override
    public void registerDataSource(RAGDataSource dataSource) {
        if (dataSource != null) {
            dataSourceMap.put(dataSource.getType(), dataSource);
            log.info("注册数据源: {} - {}", dataSource.getType(), dataSource.getDescription());
        }
    }
    
    @Override
    public List<RAGDataSource> getRegisteredDataSources() {
        return new ArrayList<>(dataSourceMap.values());
    }
    
    @Override
    public void addProduct(Product product) {
        log.info("添加商品到向量数据库: {}", product.getId());
        if (!shouldUseVectorSearch()) return;
        upsertProductToVectorStore(product);
    }
    
    @Override
    public void updateProduct(Product product) {
        log.info("更新商品向量: {}", product.getId());
        if (!shouldUseVectorSearch()) return;
        upsertProductToVectorStore(product);
    }
    
    @Override
    public void deleteProduct(Long productId) {
        log.info("删除商品向量: {}", productId);
        if (!isVectorEnabled() || vectorStore == null) return;
        try {
            vectorStore.delete(Collections.singletonList(String.valueOf(productId)));
        } catch (Exception e) {
            log.warn("删除向量失败: productId={}, err={}", productId, e.getMessage());
        }
    }
    
    /**
     * 将Map转换为Product对象
     */
    private Product mapToProduct(Map<String, Object> map) {
        if (map == null || !"product".equals(map.get("type"))) {
            return null;
        }
        
        Product product = new Product();
        product.setId(((Number) map.get("id")).longValue());
        product.setName((String) map.get("name"));
        product.setDescription((String) map.get("description"));
        product.setPrice((java.math.BigDecimal) map.get("price"));
        product.setCategoryId(map.get("categoryId") != null ? ((Number) map.get("categoryId")).longValue() : null);
        product.setStock(map.get("stock") != null ? ((Number) map.get("stock")).intValue() : null);
        product.setImageUrl((String) map.get("imageUrl"));
        product.setSpecs((String) map.get("specs"));
        return product;
    }

    private boolean isVectorEnabled() {
        return ragConfig != null && Boolean.TRUE.equals(ragConfig.getVectorEnabled());
    }

    private boolean shouldUseVectorSearch() {
        if (!isVectorEnabled()) return false;
        if (ragConfig == null) return false;
        String mode = ragConfig.getSearchMode();
        if (mode == null) mode = "keyword";
        return "vector".equalsIgnoreCase(mode) && embeddingService != null && vectorStore != null;
    }

    private List<Map<String, Object>> vectorSearchProducts(String query, int topK) {
        try {
            List<Float> qVec = embeddingService.embed(query);
            if (qVec == null || qVec.isEmpty()) return new ArrayList<>();
            List<String> ids = vectorStore.query(qVec, topK);
            if (ids == null || ids.isEmpty()) return new ArrayList<>();

            // 从MySQL取回完整商品数据（保持返回顺序）
            List<Long> productIds = ids.stream().map(id -> {
                try { return Long.parseLong(id); } catch (Exception e) { return null; }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            if (productIds.isEmpty()) return new ArrayList<>();
            List<Product> products = productMapper.selectBatchIds(productIds)
                    .stream()
                    .filter(p -> p != null && p.getStatus() != null && p.getStatus() == 1)
                    .collect(Collectors.toList());
            Map<Long, Product> map = products.stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

            List<Map<String, Object>> result = new ArrayList<>();
            for (Long pid : productIds) {
                Product p = map.get(pid);
                if (p != null) result.add(productToMap(p));
            }
            return result;
        } catch (Exception e) {
            log.warn("向量检索失败，降级关键词检索: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void syncAllProductsToVectorStore() {
        if (!isVectorEnabled() || embeddingService == null || vectorStore == null) return;
        List<Product> products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
        );
        if (products == null || products.isEmpty()) return;

        List<String> ids = new ArrayList<>();
        List<String> docs = new ArrayList<>();
        List<Map<String, Object>> metas = new ArrayList<>();

        for (Product p : products) {
            ids.add(String.valueOf(p.getId()));
            docs.add(buildProductText(p));
            Map<String, Object> meta = new HashMap<>();
            meta.put("type", "product");
            meta.put("id", p.getId());
            if (p.getCategoryId() != null) meta.put("categoryId", p.getCategoryId());
            if (p.getPrice() != null) meta.put("price", p.getPrice().doubleValue());
            if (p.getStock() != null) meta.put("stock", p.getStock());
            metas.add(meta);
        }

        List<List<Float>> vectors = embeddingService.embedBatch(docs);
        if (vectors == null || vectors.size() != ids.size()) {
            throw new RuntimeException("Embedding批量结果数量不匹配: expected=" + ids.size() + ", actual=" + (vectors == null ? 0 : vectors.size()));
        }
        vectorStore.upsert(ids, vectors, docs, metas);
        log.info("商品向量同步完成: count={}", ids.size());
    }

    private void upsertProductToVectorStore(Product p) {
        if (p == null) return;
        if (embeddingService == null || vectorStore == null) return;
        if (p.getStatus() == null || p.getStatus() != 1) return;

        String id = String.valueOf(p.getId());
        String doc = buildProductText(p);
        List<Float> vec = embeddingService.embed(doc);
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "product");
        meta.put("id", p.getId());
        if (p.getCategoryId() != null) meta.put("categoryId", p.getCategoryId());
        if (p.getPrice() != null) meta.put("price", p.getPrice().doubleValue());
        if (p.getStock() != null) meta.put("stock", p.getStock());
        vectorStore.upsert(Collections.singletonList(id), Collections.singletonList(vec), Collections.singletonList(doc), Collections.singletonList(meta));
    }

    private String buildProductText(Product p) {
        StringBuilder sb = new StringBuilder();
        if (p.getName() != null) sb.append(p.getName()).append(" ");
        if (p.getDescription() != null) sb.append(p.getDescription()).append(" ");
        if (p.getSpecs() != null) sb.append("规格:").append(p.getSpecs()).append(" ");
        if (p.getPrice() != null) sb.append("价格:").append(p.getPrice()).append(" ");
        if (p.getStock() != null) sb.append("库存:").append(p.getStock()).append(" ");
        return sb.toString().trim();
    }

    private Map<String, Object> productToMap(Product product) {
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
