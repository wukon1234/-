package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话会话实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
public class Conversation extends BaseEntity {
    
    private Long userId;
    
    private String sessionId;
    
    private String title;
}
