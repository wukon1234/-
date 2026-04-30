package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 智能助手回复模板实体
 */
@Data
@TableName("assistant_template")
public class AssistantTemplate extends BaseEntity {

    private String keyword;

    private String response;
}
