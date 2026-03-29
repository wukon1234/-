package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AddCartRequest;
import com.mall.dto.CartItemDTO;
import com.mall.dto.UpdateCartRequest;
import com.mall.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<?> addCart(
            @RequestAttribute("userId") Long userId,
            @Validated @RequestBody AddCartRequest request) {
        log.info("添加购物车: userId={}, productId={}, quantity={}", userId, request.getProductId(), request.getQuantity());
        cartService.addCart(userId, request);
        return Result.success("添加成功");
    }
    
    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItemDTO>> getCartList(@RequestAttribute("userId") Long userId) {
        List<CartItemDTO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{cartId}")
    public Result<?> updateCartQuantity(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long cartId,
            @Validated @RequestBody UpdateCartRequest request) {
        log.info("更新购物车: userId={}, cartId={}, quantity={}", userId, cartId, request.getQuantity());
        cartService.updateCartQuantity(userId, cartId, request);
        return Result.success("更新成功");
    }
    
    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{cartId}")
    public Result<?> deleteCartItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long cartId) {
        log.info("删除购物车: userId={}, cartId={}", userId, cartId);
        cartService.deleteCartItem(userId, cartId);
        return Result.success("删除成功");
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<?> clearCart(@RequestAttribute("userId") Long userId) {
        log.info("清空购物车: userId={}", userId);
        cartService.clearCart(userId);
        return Result.success("清空成功");
    }
    // TODO: 2026/1/13 购物车商品总费用
}

