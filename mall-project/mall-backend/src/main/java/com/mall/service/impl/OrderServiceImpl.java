package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.dto.CreateOrderRequest;
import com.mall.dto.OrderDTO;
import com.mall.dto.OrderItemDTO;
import com.mall.entity.Cart;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.AddressMapper;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import com.mall.entity.Address;
import com.mall.service.CartService;
import com.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private CartService cartService;
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // 获取购物车项
        List<com.mall.dto.CartItemDTO> cartItems = cartService.getCartItemsByIds(userId, request.getCartItemIds());
        
        if (cartItems.isEmpty()) {
            throw new BusinessException(400, "购物车项不能为空");
        }
        
        // 校验库存并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (com.mall.dto.CartItemDTO cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
            }
            
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(400, "商品 " + product.getName() + " 已下架");
            }
            
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(400, "商品 " + product.getName() + " 库存不足");
            }
            
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);  // 待支付

        // 处理收货地址：优先使用请求传入；否则使用默认地址
        Long addressId = request.getAddressId();
        if (addressId == null) {
            Address defaultAddress = addressMapper.selectOne(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, userId)
                            .eq(Address::getIsDefault, 1)
                            .last("LIMIT 1")
            );
            if (defaultAddress != null) {
                addressId = defaultAddress.getId();
            }
        }
        if (addressId != null) {
            // 校验地址归属
            Address address = addressMapper.selectOne(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getId, addressId)
                            .eq(Address::getUserId, userId)
            );
            if (address == null) {
                throw new BusinessException(400, "收货地址不存在或不属于当前用户");
            }
        }
        order.setAddressId(addressId);
        order.setPayTime(null);
        order.setDeliveryTime(null);
        order.setCompleteTime(null);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        
        // 创建订单项并扣减库存
        for (com.mall.dto.CartItemDTO cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
            
            // 扣减库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
        }
        
        // 删除购物车项
        cartMapper.delete(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, request.getCartItemIds())
        );
        
        log.info("创建订单成功: orderId={}, orderNo={}, userId={}, totalAmount={}", 
                order.getId(), order.getOrderNo(), userId, totalAmount);
        
        // 返回订单详情
        return getOrderDetail(userId, order.getId());
    }
    
    @Override
    public List<OrderDTO> getOrderList(Long userId, Integer status) {
        // 查询订单列表
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
            .eq(Order::getUserId, userId)
            .orderByDesc(Order::getCreateTime);
        
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        
        List<Order> orders = orderMapper.selectList(queryWrapper);
        
        // 转换为DTO并填充订单项
        return orders.stream().map(order -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(order.getId());
            dto.setOrderNo(order.getOrderNo());
            dto.setUserId(order.getUserId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setAddressId(order.getAddressId());
            dto.setCreateTime(order.getCreateTime());
            dto.setUpdateTime(order.getUpdateTime());
            dto.setPayTime(order.getPayTime());
            dto.setDeliveryTime(order.getDeliveryTime());
            dto.setCompleteTime(order.getCompleteTime());

            // 地址信息（可选）
            if (order.getAddressId() != null) {
                Address address = addressMapper.selectById(order.getAddressId());
                if (address != null) {
                    com.mall.dto.AddressDTO addressDTO = new com.mall.dto.AddressDTO();
                    BeanUtils.copyProperties(address, addressDTO);
                    addressDTO.setFullAddress(String.format("%s%s%s%s",
                            nullSafe(address.getProvince()),
                            nullSafe(address.getCity()),
                            nullSafe(address.getDistrict()),
                            nullSafe(address.getDetail())));
                    dto.setAddress(addressDTO);
                }
            }
            
            // 查询订单项
            List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId())
            );
            
            // 转换为订单项DTO
            List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
                OrderItemDTO itemDTO = new OrderItemDTO();
                BeanUtils.copyProperties(item, itemDTO);
                itemDTO.setSubtotal(calculateSubtotal(item.getPrice(), item.getQuantity()));
                return itemDTO;
            }).collect(Collectors.toList());
            
            dto.setItems(itemDTOs);
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public OrderDTO getOrderDetail(Long userId, Long orderId) {
        // 查询订单
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId)
        );
        
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 转换为DTO
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setAddressId(order.getAddressId());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        dto.setPayTime(order.getPayTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setCompleteTime(order.getCompleteTime());

        if (order.getAddressId() != null) {
            Address address = addressMapper.selectById(order.getAddressId());
            if (address != null) {
                com.mall.dto.AddressDTO addressDTO = new com.mall.dto.AddressDTO();
                BeanUtils.copyProperties(address, addressDTO);
                addressDTO.setFullAddress(String.format("%s%s%s%s",
                        nullSafe(address.getProvince()),
                        nullSafe(address.getCity()),
                        nullSafe(address.getDistrict()),
                        nullSafe(address.getDetail())));
                dto.setAddress(addressDTO);
            }
        }
        
        // 查询订单项
        List<OrderItem> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
        );
        
        // 转换为订单项DTO
        List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(item, itemDTO);
            itemDTO.setSubtotal(calculateSubtotal(item.getPrice(), item.getQuantity()));
            return itemDTO;
        }).collect(Collectors.toList());
        
        dto.setItems(itemDTOs);
        return dto;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private BigDecimal calculateSubtotal(BigDecimal price, Integer quantity) {
        BigDecimal safePrice = price == null ? BigDecimal.ZERO : price;
        int safeQuantity = quantity == null ? 0 : quantity;
        return safePrice.multiply(BigDecimal.valueOf(safeQuantity));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        // 查询订单
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId)
        );
        
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 只有待支付状态才能取消
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 恢复库存
        List<OrderItem> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
        );
        
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
            }
        }
        
        // 更新订单状态
        order.setStatus(4);  // 已取消
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        log.info("取消订单: orderId={}", orderId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        // 查询订单
        Order order = orderMapper.selectOne(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId)
        );
        
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        
        // 只有待支付状态才能支付
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        
        // 更新订单状态
        order.setStatus(1);  // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        log.info("订单支付成功: orderId={}", orderId);
    }
}

