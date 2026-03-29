package com.mall.service.assistant.impl;

import com.alibaba.fastjson2.JSON;
import com.mall.service.assistant.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
            log.info("开始调用通义千问API");
            log.info("API密钥: {}", apiKey.substring(0, 5) + "..." + apiKey.substring(apiKey.length() - 5));
            log.info("API端点: {}", baseUrl + "/v1/services/aigc/text-generation/generation");
            
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(baseUrl + "/v1/services/aigc/text-generation/generation");
            
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
            
            // 构建input对象
            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);
            requestBody.put("input", input);
            
            String requestJson = JSON.toJSONString(requestBody);
            log.info("请求体: {}", requestJson);
            
            httpPost.setEntity(new StringEntity(requestJson, "UTF-8"));
            
            log.info("发送API请求");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            log.info("API请求返回状态码: {}", response.getStatusLine().getStatusCode());
            
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("API响应: {}", responseBody);
            
            httpClient.close();
            
            // 解析响应
            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("API响应体为空");
                return "抱歉，我暂时无法回答您的问题，请稍后再试。";
            }
            
            Map<String, Object> result = JSON.parseObject(responseBody, Map.class);
            if (result == null) {
                log.warn("解析API响应失败，结果为null");
                return "抱歉，我暂时无法回答您的问题，请稍后再试。";
            }
            
            // 解析通义千问API响应格式
            Map<String, Object> output = (Map<String, Object>) result.get("output");
            if (output != null) {
                String text = (String) output.get("text");
                if (text != null && !text.isEmpty()) {
                    log.info("获取到模型回复: {}", text);
                    return text;
                }
            }
            
            log.warn("API响应中没有找到有效的回复");
            return "抱歉，我暂时无法回答您的问题，请稍后再试。";
        } catch (Exception e) {
            log.error("调用通义千问API失败，错误信息: {}", e.getMessage());
            log.error("完整错误堆栈:", e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
    }
    
    @Override
    public void chatStream(String prompt, List<Map<String, String>> history, StreamCallback callback) {
        new Thread(() -> {
            CloseableHttpClient httpClient = null;
            CloseableHttpResponse response = null;
            try {
                log.info("开始调用通义千问流式API");
                log.info("API密钥: {}", apiKey.substring(0, 5) + "..." + apiKey.substring(apiKey.length() - 5));
                log.info("API端点: {}", baseUrl + "/v1/services/aigc/text-generation/generation");
                
                httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(baseUrl + "/v1/services/aigc/text-generation/generation");
                
                // 构建请求头
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + apiKey);
                
                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", "qwen-turbo");
                requestBody.put("stream", true); // 启用流式输出
                
                List<Map<String, String>> messages = new java.util.ArrayList<>();
                if (history != null) {
                    messages.addAll(history);
                }
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);
                messages.add(userMessage);
                
                // 构建input对象
                Map<String, Object> input = new HashMap<>();
                input.put("messages", messages);
                requestBody.put("input", input);
                
                String requestJson = JSON.toJSONString(requestBody);
                log.info("请求体: {}", requestJson);
                
                httpPost.setEntity(new StringEntity(requestJson, "UTF-8"));
                
                log.info("发送API请求");
                response = httpClient.execute(httpPost);
                log.info("API请求返回状态码: {}", response.getStatusLine().getStatusCode());
                
                // 处理流式响应
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (InputStream inputStream = entity.getContent()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        String line;
                        StringBuilder fullResponse = new StringBuilder();
                        
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                continue;
                            }
                            
                            // 处理通义千问的流式响应格式
                            try {
                                Map<String, Object> result = JSON.parseObject(line, Map.class);
                                Map<String, Object> output = (Map<String, Object>) result.get("output");
                                if (output != null) {
                                    String text = (String) output.get("text");
                                    if (text != null && !text.isEmpty()) {
                                        // 只发送新增的部分
                                        if (fullResponse.length() < text.length()) {
                                            String newChunk = text.substring(fullResponse.length());
                                            fullResponse.append(newChunk);
                                            callback.onMessage(newChunk);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("解析流式响应失败: {}", e.getMessage());
                            }
                        }
                        
                        reader.close();
                    }
                }
                
                callback.onComplete();
            } catch (Exception e) {
                log.error("调用通义千问流式API失败，错误信息: {}", e.getMessage());
                log.error("完整错误堆栈:", e);
                callback.onError(e);
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                    if (httpClient != null) {
                        httpClient.close();
                    }
                } catch (IOException e) {
                    log.error("关闭HTTP连接失败: {}", e.getMessage());
                }
            }
        }).start();
    }
}
