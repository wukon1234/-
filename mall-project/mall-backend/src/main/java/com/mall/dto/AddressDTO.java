package com.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址DTO
 */
@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 便于前端展示的完整地址
     */
    private String fullAddress;
}


