package com.mall.dto;

import lombok.Data;

/**
 * AI内容检测响应DTO
 */
@Data
public class AIContentCheckResponseDTO {
    /**
     * 是否违规
     */
    private boolean isViolation;
    
    /**
     * 违规类型：violence（暴力）、pornography（色情）、abuse（辱骂）、advertisement（广告）、political（政治敏感）
     */
    private String violationType;
    
    /**
     * 建议处理方式
     */
    private String suggestedAction;
    
    /**
     * 检测结果描述
     */
    private String description;
}
