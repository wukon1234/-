package com.mall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mall.dto.AIContentCheckRequestDTO;
import com.mall.dto.AIContentCheckResponseDTO;
import com.mall.service.AIContentCheckService;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI内容检测服务实现
 */
@Slf4j
@Service
public class AIContentCheckServiceImpl implements AIContentCheckService {

    @Autowired
    private LLMService llmService;

    @Override
    public AIContentCheckResponseDTO checkContent(AIContentCheckRequestDTO request) {
        log.info("开始检测内容: {}", request.getContent());
        
        // 构建检测提示词
        String prompt = buildCheckPrompt(request.getContent());
        
        // 调用LLM进行检测
        String response = llmService.chat(prompt, null);
        log.info("LLM响应: {}", response);
        
        // 解析LLM响应
        AIContentCheckResponseDTO result = parseLLMResponse(response);
        log.info("检测结果: {}", result);
        
        return result;
    }

    @Override
    public boolean isContentViolation(String content) {
        AIContentCheckRequestDTO request = new AIContentCheckRequestDTO();
        request.setContent(content);
        request.setContentType("general");
        
        AIContentCheckResponseDTO response = checkContent(request);
        return response.isViolation();
    }

    /**
     * 构建内容检测提示词
     */
    private String buildCheckPrompt(String content) {
        return "请对以下文本进行内容安全检测，判断是否包含违规内容。\n" +
               "违规类型包括：暴力、色情、辱骂、广告、政治敏感。\n" +
               "请按照以下JSON格式返回检测结果：\n" +
               "{\n" +
               "  \"isViolation\": true/false,\n" +
               "  \"violationType\": \"violence/pornography/abuse/advertisement/political\"（如果isViolation为false，此字段为空字符串）,\n" +
               "  \"suggestedAction\": \"拒绝提交/警告/忽略\"（根据违规程度建议处理方式）,\n" +
               "  \"description\": \"检测结果描述\"\n" +
               "}\n" +
               "\n" +
               "待检测文本：" + content;
    }

    /**
     * 解析LLM响应
     */
    private AIContentCheckResponseDTO parseLLMResponse(String response) {
        AIContentCheckResponseDTO result = new AIContentCheckResponseDTO();
        
        try {
            // 提取JSON部分
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}") + 1;
            
            if (startIndex != -1 && endIndex != -1) {
                String jsonStr = response.substring(startIndex, endIndex);
                JSONObject json = JSON.parseObject(jsonStr);
                
                result.setViolation(json.getBooleanValue("isViolation"));
                result.setViolationType(json.getString("violationType"));
                result.setSuggestedAction(json.getString("suggestedAction"));
                result.setDescription(json.getString("description"));
            } else {
                // 如果无法解析JSON，默认返回安全
                result.setViolation(false);
                result.setViolationType("");
                result.setSuggestedAction("忽略");
                result.setDescription("内容安全");
            }
        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage());
            // 解析失败时默认返回安全
            result.setViolation(false);
            result.setViolationType("");
            result.setSuggestedAction("忽略");
            result.setDescription("内容安全");
        }
        
        return result;
    }
}
