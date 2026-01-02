package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息DTO
 */
@Data
public class MessageDTO {
    
    private Long id;
    
    private Integer role;  // 1-用户 2-助手
    
    private String content;
    
    private List<Long> relatedProducts;  // 相关商品ID列表
    
    private LocalDateTime createTime;
}

