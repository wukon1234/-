package com.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项DTO
 */
@Data
public class OrderItemDTO {
    
    private Long id;
    
    private Long orderId;
    
    private Long productId;
    
    private String productName;
    
    private Integer quantity;
    
    private BigDecimal price;
    
    private BigDecimal subtotal;  // 小计金额
}

