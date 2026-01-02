package com.mall.service.assistant;

import com.mall.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * RAG检索服务接口
 */
public interface RAGService {
    
    /**
     * 根据用户问题检索相关商品
     * 
     * @param query 用户问题
     * @param topK 返回最相关的K个结果
     * @return 相关商品列表
     */
    List<Product> searchProducts(String query, int topK);
    
    /**
     * 从指定数据源检索数据
     * 
     * @param query 用户问题
     * @param dataSource 数据源类型
     * @param topK 返回最相关的K个结果
     * @param filters 过滤条件
     * @return 搜索结果列表
     */
    List<Map<String, Object>> search(String query, DataSource dataSource, int topK, Map<String, Object> filters);
    
    /**
     * 从多个数据源检索数据
     * 
     * @param query 用户问题
     * @param dataSources 数据源类型列表
     * @param topK 每个数据源返回的数量
     * @param filters 过滤条件
     * @return 搜索结果Map，key为数据源类型，value为结果列表
     */
    Map<DataSource, List<Map<String, Object>>> searchMulti(String query, List<DataSource> dataSources, int topK, Map<String, Object> filters);
    
    /**
     * 注册数据源
     * 
     * @param dataSource 数据源实现
     */
    void registerDataSource(RAGDataSource dataSource);
    
    /**
     * 获取所有已注册的数据源
     * 
     * @return 数据源列表
     */
    List<RAGDataSource> getRegisteredDataSources();
    
    /**
     * 添加商品到向量数据库
     * 
     * @param product 商品信息
     */
    void addProduct(Product product);
    
    /**
     * 更新商品向量
     * 
     * @param product 商品信息
     */
    void updateProduct(Product product);
    
    /**
     * 删除商品向量
     * 
     * @param productId 商品ID
     */
    void deleteProduct(Long productId);
    
    /**
     * 初始化向量数据库（创建集合等）
     */
    void init();
}
