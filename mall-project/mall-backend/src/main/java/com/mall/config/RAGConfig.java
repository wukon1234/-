package com.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * RAG配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RAGConfig {
    
    /**
     * 启用的数据源列表
     */
    private List<String> enabledSources;
    
    /**
     * 默认检索的数据源
     */
    private String defaultSource = "product";
    
    /**
     * 是否启用多数据源检索
     */
    private Boolean multiSourceEnabled = false;

    /**
     * 检索模式：keyword / vector
     * - keyword: 关键词匹配（默认，零依赖）
     * - vector: 语义检索（依赖Embedding + 向量库）
     */
    private String searchMode = "keyword";

    /**
     * 是否开启向量检索能力（开启后仍可通过searchMode控制是否使用）
     */
    private Boolean vectorEnabled = false;

    /**
     * 启动时是否自动同步商品向量（适合数据量小的毕设演示环境）
     */
    private Boolean vectorAutoSync = false;
}

