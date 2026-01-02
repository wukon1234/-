package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
}

