package com.mall.dto;

import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    
    /**
     * 会话ID（可选，新建会话时不传）
     */
    private String sessionId;
    
    /**
     * 用户消息内容
     */
    private String message;
}
