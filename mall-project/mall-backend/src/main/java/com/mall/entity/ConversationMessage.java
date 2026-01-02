package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation_message")
public class ConversationMessage extends BaseEntity {
    
    private Long conversationId;
    
    private Integer role;  // 1-用户 2-助手
    
    private String content;
    
    private String relatedProducts;  // JSON格式，相关商品ID
}
