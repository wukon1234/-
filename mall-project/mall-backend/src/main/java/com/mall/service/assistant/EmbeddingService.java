package com.mall.service.assistant;

import java.util.List;

/**
 * Embedding服务接口（文本向量化）
 */
public interface EmbeddingService {
    
    /**
     * 将文本转换为向量
     * 
     * @param text 文本内容
     * @return 向量数组
     */
    List<Float> embed(String text);
    
    /**
     * 批量将文本转换为向量
     * 
     * @param texts 文本列表
     * @return 向量列表
     */
    List<List<Float>> embedBatch(List<String> texts);
    
    /**
     * 获取向量维度
     * 
     * @return 向量维度
     */
    int getDimension();
}
