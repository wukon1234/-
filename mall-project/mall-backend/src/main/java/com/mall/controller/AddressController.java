package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.SaveAddressRequest;
import com.mall.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 收货地址控制器
 */
@Slf4j
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public Result<?> list(@RequestAttribute("userId") Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return Result.success(addressService.getById(userId, id));
    }

    @PostMapping
    public Result<?> create(@RequestAttribute("userId") Long userId, @Validated @RequestBody SaveAddressRequest request) {
        Long id = addressService.create(userId, request);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    public Result<?> update(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Validated @RequestBody SaveAddressRequest request) {
        addressService.update(userId, id, request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        addressService.delete(userId, id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        addressService.setDefault(userId, id);
        return Result.success("设置成功");
    }

    @GetMapping("/default")
    public Result<?> getDefault(@RequestAttribute("userId") Long userId) {
        return Result.success(addressService.getDefault(userId));
    }
}


