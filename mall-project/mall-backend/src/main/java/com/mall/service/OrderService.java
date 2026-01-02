package com.mall.service;

import com.mall.dto.CreateOrderRequest;
import com.mall.dto.OrderDTO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     */
    OrderDTO createOrder(Long userId, CreateOrderRequest request);
    
    /**
     * 获取订单列表
     */
    List<OrderDTO> getOrderList(Long userId, Integer status);
    
    /**
     * 获取订单详情
     */
    OrderDTO getOrderDetail(Long userId, Long orderId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long userId, Long orderId);
    
    /**
     * 支付订单（模拟支付）
     */
    void payOrder(Long userId, Long orderId);
}

