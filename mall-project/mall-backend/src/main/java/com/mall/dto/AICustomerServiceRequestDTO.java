package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * AI客服回复请求DTO
 */
@Data
public class AICustomerServiceRequestDTO {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "场景不能为空")
    private String scene;
    
    private Map<String, String> params;
}
