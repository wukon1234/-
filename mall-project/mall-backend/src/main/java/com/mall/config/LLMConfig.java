package com.mall.config;

import com.mall.service.assistant.LLMService;
import com.mall.service.assistant.impl.OpenAILLMServiceImpl;
import com.mall.service.assistant.impl.QwenLLMServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LLM服务配置类
 */
@Slf4j
@Configuration
public class LLMConfig {
    
    @Value("${llm.provider:qwen}")
    private String provider;
    
    @Bean
    @Primary
    @ConditionalOnProperty(name = "llm.provider", havingValue = "qwen", matchIfMissing = true)
    public LLMService qwenLLMService() {
        log.info("使用通义千问作为大模型服务提供商");
        return new QwenLLMServiceImpl();
    }
    
    // 可以添加其他LLM服务的Bean配置

    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
    public LLMService openaiLLMService() {
        log.info("使用OpenAI兼容模式作为大模型服务提供商");
        return new OpenAILLMServiceImpl();
    }
}
