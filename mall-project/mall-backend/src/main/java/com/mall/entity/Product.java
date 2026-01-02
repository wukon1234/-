package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    
    private String name;
    
    private String description;
    
    private Long categoryId;
    
    private BigDecimal price;
    
    private Integer stock;
    
    private String imageUrl;
    
    private String detailImages;  // JSON格式
    
    private String specs;  // JSON格式
    
    private Integer status;  // 1-上架 0-下架
    
    private String vectorId;  // 向量数据库ID
}
