package com.mall.dto;

import com.mall.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * 聊天响应DTO
 */
@Data
public class ChatResponse {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 助手回复内容
     */
    private String message;
    
    /**
     * 相关商品列表
     */
    private List<Product> relatedProducts;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 是否流式输出
     */
    private Boolean streaming;
}
