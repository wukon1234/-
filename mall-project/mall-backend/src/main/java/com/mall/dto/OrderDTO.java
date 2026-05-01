package com.mall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO（包含订单项信息）
 */
@Data
public class OrderDTO {
    
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    private Integer status;  // 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消
    
    private Long addressId;

    /**
     * 订单关联的地址信息（便于展示）
     */
    private AddressDTO address;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime completeTime;
    
    private List<OrderItemDTO> items;
}

