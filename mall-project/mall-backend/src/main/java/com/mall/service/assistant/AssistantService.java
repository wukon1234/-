package com.mall.service.assistant;

import com.mall.dto.ChatRequest;
import com.mall.dto.ChatResponse;
import com.mall.dto.ConversationDTO;
import com.mall.dto.MessageDTO;
import com.mall.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 智能助手服务接口
 */
public interface AssistantService {
    
    /**
     * 发送消息并获取回复
     * 
     * @param userId 用户ID
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(Long userId, ChatRequest request);
    
    /**
     * 流式发送消息
     * 
     * @param userId 用户ID
     * @param request 聊天请求
     * @param callback 流式回调
     */
    void chatStream(Long userId, ChatRequest request, StreamCallback callback);
    
    /**
     * 获取会话列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 会话列表和总数
     */
    Map<String, Object> getConversations(Long userId, Integer page, Integer size);
    
    /**
     * 获取会话历史消息
     * 
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<MessageDTO> getMessages(String sessionId);
    
    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    void deleteConversation(String sessionId);
    
    /**
     * 流式回调接口
     */
    interface StreamCallback {
        void onMessage(String chunk);
        default void onComplete() {}
        default void onError(Exception e) {}
        default void onProducts(List<Product> products) {}
    }
}
