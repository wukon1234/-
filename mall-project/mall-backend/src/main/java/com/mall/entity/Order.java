package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    private Integer status;  // 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消
    
    private Long addressId;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime completeTime;
}
