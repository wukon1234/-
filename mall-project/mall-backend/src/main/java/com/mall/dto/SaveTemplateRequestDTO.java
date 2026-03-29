package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 保存模板请求DTO
 */
@Data
public class SaveTemplateRequestDTO {
    
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    
    @NotBlank(message = "场景不能为空")
    private String scene;
    
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
    
    @NotNull(message = "创建人ID不能为空")
    private Long createBy;
    
    private Integer isCommon = 0;
}
