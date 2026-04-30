package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 智能助手设置实体
 */
@Data
@TableName("assistant_settings")
public class AssistantSettings extends BaseEntity {

    private String name;

    private Integer enabled;

    private String responseMode;

    private Integer timeout;
}
