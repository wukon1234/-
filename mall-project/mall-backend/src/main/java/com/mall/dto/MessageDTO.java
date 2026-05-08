package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息DTO
 */
@Data
public class MessageDTO {
    
    private Long id;
    
    private Integer role;  // 1-用户 2-助手
    
    private String content;
    
    private List<Long> relatedProducts;  // 相关商品ID列表

    /**
     * 关联订单检索结果（与对话 RAG 一致）
     */
    private List<Map<String, Object>> relatedOrders;
    
    private LocalDateTime createTime;
}

