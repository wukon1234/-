package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客服回复记录实体类
 */
@Data
@TableName("customer_service_reply")
public class CustomerServiceReply extends BaseEntity {
    
    private Long userId;
    
    private Long adminId;
    
    private String scene;
    
    private String params;
    
    private String replyContent;
}
