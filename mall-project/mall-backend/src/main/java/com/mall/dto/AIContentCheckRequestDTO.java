package com.mall.dto;

import lombok.Data;

/**
 * AI内容检测请求DTO
 */
@Data
public class AIContentCheckRequestDTO {
    /**
     * 待检测的文本内容
     */
    private String content;
    
    /**
     * 内容类型：comment（评论）、assistant（智能助手）、announcement（公告）
     */
    private String contentType;
}
