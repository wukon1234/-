package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.BusinessException;
import com.mall.common.ResultCode;
import com.mall.dto.AIProductRequestDTO;
import com.mall.dto.AIProductResponseDTO;
import com.mall.entity.Product;
import com.mall.mapper.ProductMapper;
import com.mall.service.ProductService;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private LLMService llmService;
    
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
    
    @Override
    public AIProductResponseDTO generateProductContent(AIProductRequestDTO request) {
        try {
            // 构建提示词
            String prompt = buildProductContentPrompt(request);
            
            // 调用LLM服务生成商品文案
            String response = llmService.chat(prompt, null);
            
            // 解析生成结果
            AIProductResponseDTO result = parseProductContentResponse(response);
            
            return result;
        } catch (Exception e) {
            log.error("生成商品文案失败", e);
            throw new BusinessException("生成商品文案失败，请稍后重试");
        }
    }
    
    /**
     * 构建商品文案提示词
     */
    private String buildProductContentPrompt(AIProductRequestDTO request) {
        return "请为以下商品生成电商化、吸引人、符合商城调性的商品文案：\n" +
                "商品名称：" + request.getProductName() + "\n" +
                "关键词：" + request.getKeywords() + "\n" +
                "适用人群：" + request.getTargetAudience() + "\n" +
                "\n" +
                "请按照以下格式生成：\n" +
                "【商品标题】：生成一个吸引人的商品标题\n" +
                "【商品简介】：生成一段简短的商品简介\n" +
                "【商品详细描述】：生成详细的商品描述，包括商品特点、功能、使用方法等\n" +
                "【营销卖点】：生成3-5个营销卖点，突出商品优势\n" +
                "\n" +
                "要求：\n" +
                "1. 文案风格电商化、吸引人、符合商城调性\n" +
                "2. 内容要具体、生动，突出商品特色\n" +
                "3. 语言简洁明了，易于理解\n" +
                "4. 避免使用过于专业的术语\n" +
                "5. 确保内容符合法律法规和道德规范\n";
    }
    
    /**
     * 解析商品文案响应
     */
    private AIProductResponseDTO parseProductContentResponse(String response) {
        AIProductResponseDTO result = new AIProductResponseDTO();
        
        // 提取商品标题
        Pattern titlePattern = Pattern.compile("【商品标题】：(.*?)(?=【|$)", Pattern.DOTALL);
        Matcher titleMatcher = titlePattern.matcher(response);
        if (titleMatcher.find()) {
            result.setTitle(titleMatcher.group(1).trim());
        }
        
        // 提取商品简介
        Pattern summaryPattern = Pattern.compile("【商品简介】：(.*?)(?=【|$)", Pattern.DOTALL);
        Matcher summaryMatcher = summaryPattern.matcher(response);
        if (summaryMatcher.find()) {
            result.setSummary(summaryMatcher.group(1).trim());
        }
        
        // 提取商品详细描述
        Pattern descriptionPattern = Pattern.compile("【商品详细描述】：(.*?)(?=【|$)", Pattern.DOTALL);
        Matcher descriptionMatcher = descriptionPattern.matcher(response);
        if (descriptionMatcher.find()) {
            result.setDescription(descriptionMatcher.group(1).trim());
        }
        
        // 提取营销卖点
        Pattern sellingPointsPattern = Pattern.compile("【营销卖点】：(.*?)(?=【|$)", Pattern.DOTALL);
        Matcher sellingPointsMatcher = sellingPointsPattern.matcher(response);
        if (sellingPointsMatcher.find()) {
            result.setSellingPoints(sellingPointsMatcher.group(1).trim());
        }
        
        return result;
    }
}
