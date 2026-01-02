package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.CreateOrderRequest;
import com.mall.dto.OrderDTO;
import com.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderDTO> createOrder(
            @RequestAttribute("userId") Long userId,
            @Validated @RequestBody CreateOrderRequest request) {
        log.info("创建订单: userId={}, cartItemIds={}", userId, request.getCartItemIds());
        OrderDTO order = orderService.createOrder(userId, request);
        return Result.success(order);
    }
    
    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<List<OrderDTO>> getOrderList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Integer status) {
        List<OrderDTO> orderList = orderService.getOrderList(userId, status);
        return Result.success(orderList);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderDTO> getOrderDetail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderDetail(userId, orderId);
        return Result.success(order);
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public Result<?> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        log.info("取消订单: userId={}, orderId={}", userId, orderId);
        orderService.cancelOrder(userId, orderId);
        return Result.success("取消成功");
    }
    
    /**
     * 支付订单（模拟支付）
     */
    @PutMapping("/{orderId}/pay")
    public Result<?> payOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        log.info("支付订单: userId={}, orderId={}", userId, orderId);
        orderService.payOrder(userId, orderId);
        return Result.success("支付成功");
    }
}

