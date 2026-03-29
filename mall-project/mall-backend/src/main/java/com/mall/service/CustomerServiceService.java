package com.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.dto.AICustomerServiceRequestDTO;
import com.mall.dto.AICustomerServiceResponseDTO;
import com.mall.dto.SaveTemplateRequestDTO;
import com.mall.entity.CustomerServiceTemplate;
import com.mall.entity.CustomerServiceReply;

import java.util.List;

/**
 * 客服回复服务接口
 */
public interface CustomerServiceService extends IService<CustomerServiceTemplate> {
    
    /**
     * 生成客服回复话术
     * @param requestDTO 请求参数
     * @param adminId 管理员ID
     * @return 回复话术
     */
    AICustomerServiceResponseDTO generateReply(AICustomerServiceRequestDTO requestDTO, Long adminId);
    
    /**
     * 保存为常用模板
     * @param requestDTO 保存模板请求
     * @return 模板ID
     */
    Long saveTemplate(SaveTemplateRequestDTO requestDTO);
    
    /**
     * 获取所有模板
     * @return 模板列表
     */
    List<CustomerServiceTemplate> getAllTemplates();
    
    /**
     * 获取常用模板
     * @return 常用模板列表
     */
    List<CustomerServiceTemplate> getCommonTemplates();
    
    /**
     * 根据场景获取模板
     * @param scene 场景
     * @return 模板列表
     */
    List<CustomerServiceTemplate> getTemplatesByScene(String scene);
    
    /**
     * 获取回复记录
     * @param userId 用户ID
     * @return 回复记录列表
     */
    List<CustomerServiceReply> getReplyHistory(Long userId);
}
