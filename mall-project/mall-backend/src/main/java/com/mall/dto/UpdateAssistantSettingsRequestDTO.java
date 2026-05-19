package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新智能助手设置请求 DTO
 */
@Data
public class UpdateAssistantSettingsRequestDTO {

    @NotBlank(message = "助手名称不能为空")
    private String name;

    @NotNull(message = "助手状态不能为空")
    private Integer enabled;

    @NotBlank(message = "回复模式不能为空")
    private String responseMode;

    @NotNull(message = "回复超时时间不能为空")
    @Min(value = 1, message = "回复超时时间不能小于 1 秒")
    @Max(value = 60, message = "回复超时时间不能大于 60 秒")
    private Integer timeout;
}
