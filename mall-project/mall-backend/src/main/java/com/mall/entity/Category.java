package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {
    
    private String name;
    
    private Long parentId;
    
    private Integer level;
    
    private Integer sortOrder;
}
