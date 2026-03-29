package com.mall.dto;

import lombok.Data;

/**
 * AI生成商品文案响应DTO
 */
@Data
public class AIProductResponseDTO {
    /**
     * 商品标题
     */
    private String title;
    
    /**
     * 商品简介
     */
    private String summary;
    
    /**
     * 商品详细描述
     */
    private String description;
    
    /**
     * 营销卖点
     */
    private String sellingPoints;
}