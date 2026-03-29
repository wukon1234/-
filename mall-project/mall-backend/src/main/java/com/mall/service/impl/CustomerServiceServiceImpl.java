package com.mall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.dto.AICustomerServiceRequestDTO;
import com.mall.dto.AICustomerServiceResponseDTO;
import com.mall.dto.SaveTemplateRequestDTO;
import com.mall.entity.CustomerServiceTemplate;
import com.mall.entity.CustomerServiceReply;
import com.mall.mapper.CustomerServiceTemplateMapper;
import com.mall.mapper.CustomerServiceReplyMapper;
import com.mall.service.CustomerServiceService;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 客服回复服务实现类
 */
@Slf4j
@Service
public class CustomerServiceServiceImpl extends ServiceImpl<CustomerServiceTemplateMapper, CustomerServiceTemplate> implements CustomerServiceService {
    
    @Autowired
    private CustomerServiceTemplateMapper templateMapper;
    
    @Autowired
    private CustomerServiceReplyMapper replyMapper;
    
    @Autowired
    private LLMService llmService;
    
    @Override
    public AICustomerServiceResponseDTO generateReply(AICustomerServiceRequestDTO requestDTO, Long adminId) {
        log.info("开始生成客服回复话术，场景：{}", requestDTO.getScene());
        
        // 1. 根据场景获取模板
        CustomerServiceTemplate template = getTemplateByScene(requestDTO.getScene());
        if (template == null) {
            log.warn("未找到场景对应的模板：{}", requestDTO.getScene());
            return null;
        }
        
        // 2. 构建prompt
        String prompt = buildPrompt(template.getTemplateContent(), requestDTO.getParams());
        log.info("构建的prompt：{}", prompt);
        
        // 3. 调用LLM生成回复
        String replyContent = llmService.chat(prompt, null);
        log.info("生成的回复话术：{}", replyContent);
        
        // 4. 保存回复记录
        saveReplyRecord(requestDTO, adminId, replyContent);
        
        // 5. 构建响应
        AICustomerServiceResponseDTO responseDTO = new AICustomerServiceResponseDTO();
        responseDTO.setReplyContent(replyContent);
        responseDTO.setScene(requestDTO.getScene());
        responseDTO.setTemplateId(template.getId());
        
        return responseDTO;
    }
    
    @Override
    public Long saveTemplate(SaveTemplateRequestDTO requestDTO) {
        log.info("保存客服回复模板：{}", requestDTO.getTemplateName());
        
        CustomerServiceTemplate template = new CustomerServiceTemplate();
        template.setTemplateName(requestDTO.getTemplateName());
        template.setScene(requestDTO.getScene());
        template.setTemplateContent(requestDTO.getTemplateContent());
        template.setCreateBy(requestDTO.getCreateBy());
        template.setIsCommon(requestDTO.getIsCommon());
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        templateMapper.insert(template);
        log.info("模板保存成功，ID：{}", template.getId());
        
        return template.getId();
    }
    
    @Override
    public List<CustomerServiceTemplate> getAllTemplates() {
        return templateMapper.selectList(null);
    }
    
    @Override
    public List<CustomerServiceTemplate> getCommonTemplates() {
        LambdaQueryWrapper<CustomerServiceTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerServiceTemplate::getIsCommon, 1);
        return templateMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<CustomerServiceTemplate> getTemplatesByScene(String scene) {
        LambdaQueryWrapper<CustomerServiceTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerServiceTemplate::getScene, scene);
        return templateMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<CustomerServiceReply> getReplyHistory(Long userId) {
        LambdaQueryWrapper<CustomerServiceReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerServiceReply::getUserId, userId)
                .orderByDesc(CustomerServiceReply::getCreateTime);
        return replyMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据场景获取模板
     */
    private CustomerServiceTemplate getTemplateByScene(String scene) {
        LambdaQueryWrapper<CustomerServiceTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerServiceTemplate::getScene, scene)
                .eq(CustomerServiceTemplate::getIsCommon, 1)
                .last("LIMIT 1");
        return templateMapper.selectOne(queryWrapper);
    }
    
    /**
     * 构建prompt
     */
    private String buildPrompt(String templateContent, Map<String, String> params) {
        String prompt = "你是一位专业的商城客服，需要根据以下模板和参数生成标准、礼貌、专业的客服回复话术。\n\n";
        prompt += "模板：\n" + templateContent + "\n\n";
        
        if (params != null && !params.isEmpty()) {
            prompt += "参数：\n";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                prompt += entry.getKey() + "：" + entry.getValue() + "\n";
            }
        }
        
        prompt += "\n请生成符合商城客服语气的回复，要求：\n";
        prompt += "1. 礼貌、专业、简洁，不生硬\n";
        prompt += "2. 符合模板结构，但可以根据参数适当调整语言表达\n";
        prompt += "3. 确保回复中包含所有必要的信息\n";
        prompt += "4. 禁止包含敏感内容\n";
        prompt += "5. 直接输出回复内容，不需要任何引言或开场白\n";
        
        return prompt;
    }
    
    /**
     * 保存回复记录
     */
    private void saveReplyRecord(AICustomerServiceRequestDTO requestDTO, Long adminId, String replyContent) {
        CustomerServiceReply reply = new CustomerServiceReply();
        reply.setUserId(requestDTO.getUserId());
        reply.setAdminId(adminId);
        reply.setScene(requestDTO.getScene());
        reply.setParams(requestDTO.getParams() != null ? JSON.toJSONString(requestDTO.getParams()) : null);
        reply.setReplyContent(replyContent);
        reply.setCreateTime(LocalDateTime.now());
        reply.setUpdateTime(LocalDateTime.now());
        
        replyMapper.insert(reply);
        log.info("回复记录保存成功");
    }
}
