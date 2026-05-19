package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 保存智能助手模板请求 DTO
 */
@Data
public class SaveAssistantTemplateRequestDTO {

    @NotBlank(message = "关键词不能为空")
    private String keyword;

    @NotBlank(message = "回复内容不能为空")
    private String response;
}
