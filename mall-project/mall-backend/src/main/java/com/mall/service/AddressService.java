package com.mall.service;

import com.mall.dto.AddressDTO;
import com.mall.dto.SaveAddressRequest;

import java.util.List;

/**
 * 收货地址服务
 */
public interface AddressService {

    List<AddressDTO> listByUserId(Long userId);

    AddressDTO getById(Long userId, Long addressId);

    Long create(Long userId, SaveAddressRequest request);

    void update(Long userId, Long addressId, SaveAddressRequest request);

    void delete(Long userId, Long addressId);

    void setDefault(Long userId, Long addressId);

    /**
     * 获取默认地址（不存在返回null）
     */
    AddressDTO getDefault(Long userId);
}


