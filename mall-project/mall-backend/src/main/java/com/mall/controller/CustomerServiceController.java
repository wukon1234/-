package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.AICustomerServiceRequestDTO;
import com.mall.dto.AICustomerServiceResponseDTO;
import com.mall.dto.SaveTemplateRequestDTO;
import com.mall.entity.CustomerServiceTemplate;
import com.mall.entity.CustomerServiceReply;
import com.mall.service.CustomerServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 客服回复控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {
    
    @Autowired
    private CustomerServiceService customerServiceService;
    
    /**
     * 生成客服回复话术
     */
    @PostMapping("/generate-reply")
    public Result<AICustomerServiceResponseDTO> generateReply(@Valid @RequestBody AICustomerServiceRequestDTO requestDTO) {
        log.info("生成客服回复话术请求：{}，场景：{}", requestDTO.getUserId(), requestDTO.getScene());
        
        // 模拟管理员ID，实际应该从JWT中获取
        Long adminId = 1L;
        
        AICustomerServiceResponseDTO responseDTO = customerServiceService.generateReply(requestDTO, adminId);
        if (responseDTO == null) {
            return Result.error("未找到对应场景的模板");
        }
        
        return Result.success(responseDTO);
    }
    
    /**
     * 保存为常用模板
     */
    @PostMapping("/save-template")
    public Result<Long> saveTemplate(@Valid @RequestBody SaveTemplateRequestDTO requestDTO) {
        log.info("保存客服回复模板请求：{}", requestDTO.getTemplateName());
        
        Long templateId = customerServiceService.saveTemplate(requestDTO);
        return Result.success(templateId);
    }
    
    /**
     * 获取所有模板
     */
    @GetMapping("/templates")
    public Result<List<CustomerServiceTemplate>> getAllTemplates() {
        log.info("获取所有客服回复模板");
        
        List<CustomerServiceTemplate> templates = customerServiceService.getAllTemplates();
        return Result.success(templates);
    }
    
    /**
     * 获取常用模板
     */
    @GetMapping("/templates/common")
    public Result<List<CustomerServiceTemplate>> getCommonTemplates() {
        log.info("获取常用客服回复模板");
        
        List<CustomerServiceTemplate> templates = customerServiceService.getCommonTemplates();
        return Result.success(templates);
    }
    
    /**
     * 根据场景获取模板
     */
    @GetMapping("/templates/scene/{scene}")
    public Result<List<CustomerServiceTemplate>> getTemplatesByScene(@PathVariable String scene) {
        log.info("根据场景获取客服回复模板：{}", scene);
        
        List<CustomerServiceTemplate> templates = customerServiceService.getTemplatesByScene(scene);
        return Result.success(templates);
    }
    
    /**
     * 获取回复记录
     */
    @GetMapping("/history/{userId}")
    public Result<List<CustomerServiceReply>> getReplyHistory(@PathVariable Long userId) {
        log.info("获取用户客服回复记录：{}", userId);
        
        List<CustomerServiceReply> history = customerServiceService.getReplyHistory(userId);
        return Result.success(history);
    }
}
