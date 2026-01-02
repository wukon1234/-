package com.mall.service.assistant.vector;

import java.util.List;
import java.util.Map;

/**
 * 向量数据库抽象接口（可替换为Chroma/Milvus/pgvector等）
 */
public interface VectorStore {

    /**
     * 初始化（创建集合等）
     */
    void init();

    /**
     * upsert向量
     *
     * @param ids 唯一ID列表（建议使用业务ID）
     * @param embeddings 向量列表
     * @param documents 原始文本
     * @param metadatas 元数据（可选）
     */
    void upsert(List<String> ids, List<List<Float>> embeddings, List<String> documents, List<Map<String, Object>> metadatas);

    /**
     * 向量检索
     *
     * @param queryEmbedding 查询向量
     * @param topK 返回数量
     * @return 命中ID列表（按相关度排序）
     */
    List<String> query(List<Float> queryEmbedding, int topK);

    /**
     * 删除向量
     */
    void delete(List<String> ids);
}


