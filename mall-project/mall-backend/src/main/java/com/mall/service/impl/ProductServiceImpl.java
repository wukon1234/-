package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import com.mall.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public List<Product> getProductList(Integer page, Integer size) {
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = productMapper.selectPage(productPage, 
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1) // 只显示上架商品
                .orderByDesc(Product::getCreateTime)
        );
        log.info("获取商品列表: page={}, size={}, total={}", page, size, resultPage.getTotal());
        return resultPage.getRecords();
    }
    
    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        // 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        log.info("获取商品详情: id={}, name={}", id, product.getName());
        return product;
    }
    
    @Override
    public List<Product> searchProducts(String keyword, Integer page, Integer size) {
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = productMapper.selectPage(productPage,
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1) // 只显示上架商品
                .and(wrapper -> wrapper
                    .like(Product::getName, keyword)
                    .or().like(Product::getDescription, keyword)
                    .or().like(Product::getSpecs, keyword)
                )
                .orderByDesc(Product::getCreateTime)
        );
        log.info("商品搜索: keyword={}, page={}, size={}, total={}", keyword, page, size, resultPage.getTotal());
        return resultPage.getRecords();
    }
    
    @Override
    public List<Product> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> resultPage = productMapper.selectPage(productPage,
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1) // 只显示上架商品
                .eq(Product::getCategoryId, categoryId)
                .orderByDesc(Product::getCreateTime)
        );
        log.info("根据分类获取商品: categoryId={}, page={}, size={}, total={}", categoryId, page, size, resultPage.getTotal());
        return resultPage.getRecords();
    }
    
    @Override
    public List<Product> getHotProducts(Integer limit) {
        // 这里简单实现为获取最新上架的商品，实际应该根据销量等指标
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1) // 只显示上架商品
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit)
        );
        log.info("获取热门商品: limit={}", limit);
        return products;
    }
    
    @Override
    public List<Product> getNewProducts(Integer limit) {
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1) // 只显示上架商品
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit)
        );
        log.info("获取最新商品: limit={}", limit);
        return products;
    }
}
