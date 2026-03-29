package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客服回复模板实体类
 */
@Data
@TableName("customer_service_template")
public class CustomerServiceTemplate extends BaseEntity {
    
    private String templateName;
    
    private String scene;
    
    private String templateContent;
    
    private Long createBy;
    
    @TableField("is_common")
    private Integer isCommon;
}
