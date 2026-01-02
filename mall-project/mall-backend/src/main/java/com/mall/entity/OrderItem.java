package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单项实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {
    
    private Long orderId;
    
    private Long productId;
    
    private String productName;
    
    private Integer quantity;
    
    private BigDecimal price;
}

