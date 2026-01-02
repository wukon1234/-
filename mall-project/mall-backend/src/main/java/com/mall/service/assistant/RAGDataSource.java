package com.mall.service.assistant;

import java.util.List;
import java.util.Map;

/**
 * RAG数据源接口
 * 用于定义不同类型的数据源如何进行检索
 */
public interface RAGDataSource {
    
    /**
     * 获取数据源类型
     * 
     * @return 数据源类型
     */
    DataSource getType();
    
    /**
     * 搜索数据
     * 
     * @param query 查询文本
     * @param topK 返回数量
     * @param filters 过滤条件
     * @return 搜索结果列表
     */
    List<Map<String, Object>> search(String query, int topK, Map<String, Object> filters);
    
    /**
     * 将数据转换为用于向量化的文本
     * 
     * @param data 原始数据
     * @return 文本内容
     */
    String toText(Map<String, Object> data);
    
    /**
     * 获取数据源描述
     * 
     * @return 描述信息
     */
    String getDescription();
}

