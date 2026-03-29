package com.mall.dto;

import com.mall.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项DTO（包含商品信息）
 */
@Data
public class CartItemDTO {
    
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
    
    private Product product;
    
    private BigDecimal subtotal;  // 小计金额

    private  BigDecimal total;  //商品总价
}

