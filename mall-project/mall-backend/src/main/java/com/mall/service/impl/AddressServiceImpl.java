package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.dto.AddressDTO;
import com.mall.dto.SaveAddressRequest;
import com.mall.entity.Address;
import com.mall.mapper.AddressMapper;
import com.mall.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现
 */
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<AddressDTO> listByUserId(Long userId) {
        List<Address> list = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getUpdateTime)
                        .orderByDesc(Address::getCreateTime)
        );
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AddressDTO getById(Long userId, Long addressId) {
        Address address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, addressId)
                        .eq(Address::getUserId, userId)
        );
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        return toDTO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, SaveAddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        // 如果这是用户的第一条地址，自动设为默认
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        if (count == null || count == 0) {
            address.setIsDefault(1);
        }

        // 设为默认时，清空其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        }

        addressMapper.insert(address);
        log.info("新增地址: userId={}, addressId={}", userId, address.getId());
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long addressId, SaveAddressRequest request) {
        Address address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, addressId)
                        .eq(Address::getUserId, userId)
        );
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setUpdateTime(LocalDateTime.now());

        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId);
            address.setIsDefault(1);
        }

        addressMapper.updateById(address);
        log.info("更新地址: userId={}, addressId={}", userId, addressId);
    }

    @Override
    public void delete(Long userId, Long addressId) {
        Address address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, addressId)
                        .eq(Address::getUserId, userId)
        );
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }

        addressMapper.deleteById(addressId);
        log.info("删除地址: userId={}, addressId={}", userId, addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        Address address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, addressId)
                        .eq(Address::getUserId, userId)
        );
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    public AddressDTO getDefault(Long userId) {
        Address address = addressMapper.selectOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
                        .last("LIMIT 1")
        );
        return address == null ? null : toDTO(address);
    }

    private void clearDefault(Long userId) {
        List<Address> defaults = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
        );
        for (Address a : defaults) {
            a.setIsDefault(0);
            a.setUpdateTime(LocalDateTime.now());
            addressMapper.updateById(a);
        }
    }

    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        dto.setFullAddress(String.format("%s%s%s%s",
                nullSafe(address.getProvince()),
                nullSafe(address.getCity()),
                nullSafe(address.getDistrict()),
                nullSafe(address.getDetail())));
        return dto;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}


