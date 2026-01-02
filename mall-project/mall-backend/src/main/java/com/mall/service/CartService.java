package com.mall.service;

import com.mall.dto.AddCartRequest;
import com.mall.dto.CartItemDTO;
import com.mall.dto.UpdateCartRequest;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {
    
    /**
     * 添加商品到购物车
     */
    void addCart(Long userId, AddCartRequest request);
    
    /**
     * 获取购物车列表
     */
    List<CartItemDTO> getCartList(Long userId);
    
    /**
     * 更新购物车商品数量
     */
    void updateCartQuantity(Long userId, Long cartId, UpdateCartRequest request);
    
    /**
     * 删除购物车商品
     */
    void deleteCartItem(Long userId, Long cartId);
    
    /**
     * 清空购物车
     */
    void clearCart(Long userId);
    
    /**
     * 根据ID列表获取购物车项
     */
    List<CartItemDTO> getCartItemsByIds(Long userId, List<Long> cartItemIds);
}

