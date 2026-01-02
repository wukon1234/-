package com.mall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("address")
public class Address extends BaseEntity {

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detail;

    /**
     * 1-默认 0-非默认
     */
    private Integer isDefault;
}


