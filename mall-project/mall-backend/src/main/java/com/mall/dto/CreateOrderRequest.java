package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    
    @NotEmpty(message = "购物车项ID列表不能为空")
    private List<Long> cartItemIds;
    
    /**
     * 收货地址ID：前端建议必传；若不传，后端将尝试使用默认地址
     */
    private Long addressId;
}

