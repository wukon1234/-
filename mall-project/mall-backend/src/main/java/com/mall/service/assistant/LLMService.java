package com.mall.service.assistant;

import java.util.List;
import java.util.Map;

/**
 * 大模型服务接口
 */
public interface LLMService {
    
    /**
     * 发送消息并获取回复
     * 
     * @param prompt 提示词
     * @param history 历史对话记录
     * @return 模型回复
     */
    String chat(String prompt, List<Map<String, String>> history);
    
    /**
     * 流式发送消息并获取回复
     * 
     * @param prompt 提示词
     * @param history 历史对话记录
     * @param callback 流式回调函数
     */
    void chatStream(String prompt, List<Map<String, String>> history, StreamCallback callback);
    
    /**
     * 流式回调接口
     */
    @FunctionalInterface
    interface StreamCallback {
        void onMessage(String chunk);
        default void onComplete() {}
        default void onError(Exception e) {}
    }
}
