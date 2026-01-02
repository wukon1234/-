package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.dto.AddCartRequest;
import com.mall.dto.CartItemDTO;
import com.mall.dto.UpdateCartRequest;
import com.mall.entity.Cart;
import com.mall.entity.Product;
import com.mall.mapper.CartMapper;
import com.mall.mapper.ProductMapper;
import com.mall.service.CartService;
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
 * 购物车服务实现类
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long userId, AddCartRequest request) {
        // 查询商品是否存在
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        
        // 检查商品状态
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(400, "商品已下架");
        }
        
        // 检查库存
        if (product.getStock() == null || product.getStock() < request.getQuantity()) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
        }
        
        // 查询购物车中是否已存在该商品
        Cart existingCart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId())
        );
        
        if (existingCart != null) {
            // 如果已存在，更新数量
            Integer newQuantity = existingCart.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
            }
            existingCart.setQuantity(newQuantity);
            existingCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            log.info("更新购物车商品数量: userId={}, productId={}, quantity={}", userId, request.getProductId(), newQuantity);
        } else {
            // 如果不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
            log.info("添加购物车商品: userId={}, productId={}, quantity={}", userId, request.getProductId(), request.getQuantity());
        }
    }
    
    @Override
    public List<CartItemDTO> getCartList(Long userId) {
        // 查询购物车列表
        List<Cart> carts = cartMapper.selectList(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
        );
        
        // 转换为DTO并填充商品信息
        return carts.stream().map(cart -> {
            CartItemDTO dto = new CartItemDTO();
            BeanUtils.copyProperties(cart, dto);
            
            // 查询商品信息
            Product product = productMapper.selectById(cart.getProductId());
            dto.setProduct(product);
            
            // 计算小计
            if (product != null && product.getPrice() != null) {
                dto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartQuantity(Long userId, Long cartId, UpdateCartRequest request) {
        // 查询购物车项
        Cart cart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .eq(Cart::getUserId, userId)
        );
        
        if (cart == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        
        // 查询商品信息，检查库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
        }
        
        // 更新数量
        cart.setQuantity(request.getQuantity());
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
        log.info("更新购物车商品数量: cartId={}, quantity={}", cartId, request.getQuantity());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long userId, Long cartId) {
        // 查询购物车项
        Cart cart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .eq(Cart::getUserId, userId)
        );
        
        if (cart == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        
        // 删除
        cartMapper.deleteById(cartId);
        log.info("删除购物车商品: cartId={}", cartId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        cartMapper.delete(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
        );
        log.info("清空购物车: userId={}", userId);
    }
    
    @Override
    public List<CartItemDTO> getCartItemsByIds(Long userId, List<Long> cartItemIds) {
        // 查询购物车项
        List<Cart> carts = cartMapper.selectList(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartItemIds)
        );
        
        // 转换为DTO并填充商品信息
        return carts.stream().map(cart -> {
            CartItemDTO dto = new CartItemDTO();
            BeanUtils.copyProperties(cart, dto);
            
            // 查询商品信息
            Product product = productMapper.selectById(cart.getProductId());
            dto.setProduct(product);
            
            // 计算小计
            if (product != null && product.getPrice() != null) {
                dto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}

