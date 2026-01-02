package com.mall.service.assistant.impl;

import com.alibaba.fastjson2.JSON;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通义千问LLM服务实现
 */
@Slf4j
@Service
public class QwenLLMServiceImpl implements LLMService {
    
    @Value("${llm.qwen.api-key:}")
    private String apiKey;
    
    @Value("${llm.qwen.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;
    
    @Value("${llm.provider:qwen}")
    private String provider;
    
    @Override
    public String chat(String prompt, List<Map<String, String>> history) {
        if (!"qwen".equals(provider)) {
            return "当前配置的大模型提供商不是通义千问，请检查配置";
        }
        
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(baseUrl + "/services/aigc/text-generation/generation");
            
            // 构建请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-turbo");
            
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            if (history != null) {
                messages.addAll(history);
            }
            // 替换 Map.of 为传统的 HashMap 方式
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            // 替换 Map.of 为传统的 HashMap 方式
            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);
            requestBody.put("input", input);
            
            httpPost.setEntity(new StringEntity(JSON.toJSONString(requestBody), "UTF-8"));
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            httpClient.close();
            
            // 解析响应
            Map<String, Object> result = JSON.parseObject(responseBody, Map.class);
            Map<String, Object> output = (Map<String, Object>) result.get("output");
            if (output != null) {
                return (String) output.get("text");
            }
            
            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
        } catch (Exception e) {
            log.error("调用通义千问API失败", e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
    }
    
    @Override
    public void chatStream(String prompt, List<Map<String, String>> history, StreamCallback callback) {
        // 由于通义千问的流式API需要特殊处理，这里先模拟流式输出
        // 实际生产环境应该使用通义千问的流式API
        new Thread(() -> {
            try {
                String result = chat(prompt, history);
                
                // 模拟流式输出，将结果分块发送
                if (result != null && !result.isEmpty()) {
                    int chunkSize = 10; // 每次发送10个字符
                    for (int i = 0; i < result.length(); i += chunkSize) {
                        int end = Math.min(i + chunkSize, result.length());
                        String chunk = result.substring(i, end);
                        callback.onMessage(chunk);
                        
                        // 模拟网络延迟
                        Thread.sleep(50);
                    }
                }
                callback.onComplete();
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
