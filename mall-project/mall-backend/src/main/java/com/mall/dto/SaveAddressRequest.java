package com.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 新增/修改地址请求DTO
 */
@Data
public class SaveAddressRequest {
    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区/县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /**
     * 是否设为默认地址：1是0否（可选）
     */
    private Integer isDefault;
}


