package com.mall.dto;

import lombok.Data;

/**
 * AI生成商品文案请求DTO
 */
@Data
public class AIProductRequestDTO {
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 关键词
     */
    private String keywords;
    
    /**
     * 适用人群
     */
    private String targetAudience;
}