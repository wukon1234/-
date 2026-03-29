package com.mall.dto;

import lombok.Data;

/**
 * AI客服回复响应DTO
 */
@Data
public class AICustomerServiceResponseDTO {
    
    private String replyContent;
    
    private String scene;
    
    private Long templateId;
}
