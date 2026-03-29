package com.mall.service.assistant.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.dto.ChatRequest;
import com.mall.dto.ChatResponse;
import com.mall.dto.ConversationDTO;
import com.mall.dto.MessageDTO;
import com.mall.mapper.ConversationMapper;
import com.mall.mapper.ConversationMessageMapper;
import com.mall.entity.Conversation;
import com.mall.entity.ConversationMessage;
import com.mall.service.assistant.AssistantService;
import com.mall.service.assistant.LLMService;
import com.mall.service.assistant.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能助手服务实现类
 */
@Slf4j
@Service
public class AssistantServiceImpl implements AssistantService {
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Autowired
    private ConversationMessageMapper conversationMessageMapper;
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private RAGService ragService;
    
    @Override
    public ChatResponse chat(Long userId, ChatRequest request) {
        // 1. 获取或创建会话
        Conversation conversation = getOrCreateConversation(userId, request.getSessionId());
        
        // 2. 保存用户消息
        saveMessage(conversation.getId(), 1, request.getMessage(), null);
        
        // 3. RAG检索相关商品
        List<com.mall.entity.Product> relatedProducts = ragService.searchProducts(request.getMessage(), 5);
        
        // 4. 构建提示词
        String prompt = buildPrompt(request.getMessage(), relatedProducts, conversation.getId());
        
        // 5. 获取历史对话
        List<Map<String, String>> history = getHistoryMessages(conversation.getId(), 10);
        
        // 6. 调用大模型
        String assistantReply = llmService.chat(prompt, history);
        
        // 7. 提取商品ID列表
        List<Long> productIds = extractProductIds(relatedProducts);
        
        // 8. 保存助手回复
        Long messageId = saveMessage(conversation.getId(), 2, assistantReply, productIds);
        
        // 9. 构建响应
        ChatResponse response = new ChatResponse();
        response.setSessionId(conversation.getSessionId());
        response.setMessage(assistantReply);
        response.setRelatedProducts(relatedProducts);
        response.setMessageId(messageId);
        
        return response;
    }
    
    @Override
    public void chatStream(Long userId, ChatRequest request, StreamCallback callback) {
        new Thread(() -> {
            try {
                // 1. 获取或创建会话
                Conversation conversation = getOrCreateConversation(userId, request.getSessionId());
                
                // 2. 保存用户消息
                saveMessage(conversation.getId(), 1, request.getMessage(), null);
                
                // 3. RAG检索相关商品
                List<com.mall.entity.Product> relatedProducts = ragService.searchProducts(request.getMessage(), 5);
                
                // 4. 构建提示词
                String prompt = buildPrompt(request.getMessage(), relatedProducts, conversation.getId());
                
                // 5. 获取历史对话
                List<Map<String, String>> history = getHistoryMessages(conversation.getId(), 10);
                
                // 6. 调用大模型（流式）
                StringBuilder fullResponse = new StringBuilder();
                llmService.chatStream(prompt, history, new LLMService.StreamCallback() {
                    @Override
                    public void onMessage(String chunk) {
                        fullResponse.append(chunk);
                        callback.onMessage(chunk);
                    }
                    
                    @Override
                    public void onComplete() {
                        // 7. 保存完整回复
                        String assistantReply = fullResponse.toString();
                        List<Long> productIds = extractProductIds(relatedProducts);
                        saveMessage(conversation.getId(), 2, assistantReply, productIds);
                        
                        // 8. 发送商品推荐信息
                        if (relatedProducts != null && !relatedProducts.isEmpty()) {
                            callback.onProducts(relatedProducts);
                        }
                        
                        callback.onComplete();
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
    
    /**
     * 获取或创建会话
     */
    private Conversation getOrCreateConversation(Long userId, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            Conversation conversation = conversationMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Conversation>()
                    .eq(Conversation::getSessionId, sessionId)
            );
            if (conversation != null) {
                return conversation;
            }
        }
        
        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        conversation.setTitle("新对话");
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.insert(conversation);
        
        return conversation;
    }
    
    /**
     * 保存消息
     */
    private Long saveMessage(Long conversationId, Integer role, String content, List<Long> productIds) {
        ConversationMessage message = new ConversationMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        if (productIds != null && !productIds.isEmpty()) {
            message.setRelatedProducts(com.alibaba.fastjson2.JSON.toJSONString(productIds));
        }
        message.setCreateTime(LocalDateTime.now());
        conversationMessageMapper.insert(message);

        // 会话标题自动生成：第一条用户消息用于生成标题（避免一直显示“新对话”）
        if (role != null && role == 1) {
            try {
                Conversation conv = conversationMapper.selectById(conversationId);
                if (conv != null && (conv.getTitle() == null || "新对话".equals(conv.getTitle()))) {
                    String title = generateTitle(content);
                    if (title != null && !title.trim().isEmpty()) {
                        conv.setTitle(title);
                        conv.setUpdateTime(LocalDateTime.now());
                        conversationMapper.updateById(conv);
                    }
                }
            } catch (Exception e) {
                log.warn("会话标题生成失败: {}", e.getMessage());
            }
        }
        return message.getId();
    }

    private String generateTitle(String firstUserMessage) {
        if (firstUserMessage == null) return "新对话";
        String s = firstUserMessage.trim();
        if (s.isEmpty()) return "新对话";
        // 去掉换行，截断长度（适合会话列表展示）
        s = s.replace("\r", " ").replace("\n", " ");
        int maxLen = 14;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(String userMessage, List<com.mall.entity.Product> products, Long conversationId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的【商城智能购物助手】，只回答与本商城购物相关的问题，")
              .append("例如：商品咨询、选品对比、库存与价格、下单流程、支付方式、配送与物流、售后与退换货等。\n")
              .append("如果用户的问题与商城无关，请礼貌说明“我目前只能回答本商城相关的问题”。\n\n");

        prompt.append("以下是根据用户问题在商城中检索到的商品信息（如果有）：\n");
        if (products != null && !products.isEmpty()) {
            int index = 1;
            for (com.mall.entity.Product product : products) {
                prompt.append(String.format(
                    "%d. 商品名称：%s；简介：%s；价格：%s 元。\n",
                    index++,
                    product.getName(),
                    product.getDescription() == null ? "暂无描述" : product.getDescription(),
                    product.getPrice() == null ? "暂无定价" : product.getPrice().toPlainString()
                ));
            }
        } else {
            prompt.append("（当前未检索到相关商品，你仍然可以基于商城的一般规则进行说明和引导。）\n");
        }
        prompt.append("\n");

        prompt.append("当前用户问题：").append(userMessage).append("\n\n");
        prompt.append("回答要求：\n")
              .append("1）使用简体中文，语气友好、专业，分点或分段回答，提高可读性；\n")
              .append("2）当推荐上方列表中的商品时，请直接引用商品名称并结合价格做具体推荐理由；\n")
              .append("3）涉及下单/支付/配送/售后等流程时，请按照常见电商流程清晰分步骤说明；\n")
              .append("4）如果信息不确定，不要胡乱编造，可以给出“当前系统暂不支持自动查询，请联系客服或在订单页查看”等安全回答。\n");
        
        return prompt.toString();
    }
    
    /**
     * 获取历史消息
     */
    private List<Map<String, String>> getHistoryMessages(Long conversationId, int limit) {
        List<ConversationMessage> messages = conversationMessageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getConversationId, conversationId)
                .orderByDesc(ConversationMessage::getCreateTime)
                .last("LIMIT " + limit)
        );
        
        Collections.reverse(messages); // 反转列表，保持时间顺序
        
        List<Map<String, String>> history = new ArrayList<>();
        for (ConversationMessage msg : messages) {
            Map<String, String> map = new HashMap<>();
            map.put("role", msg.getRole() == 1 ? "user" : "assistant");
            map.put("content", msg.getContent());
            history.add(map);
        }
        
        return history;
    }
    
    /**
     * 提取商品ID列表
     */
    private List<Long> extractProductIds(List<com.mall.entity.Product> products) {
        List<Long> ids = new ArrayList<>();
        if (products != null) {
            for (com.mall.entity.Product product : products) {
                ids.add(product.getId());
            }
        }
        return ids;
    }
    
    @Override
    public Map<String, Object> getConversations(Long userId, Integer page, Integer size) {
        // 分页查询会话列表
        Page<Conversation> pageParam = new Page<>(page, size);
        Page<Conversation> conversationPage = conversationMapper.selectPage(
            pageParam,
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdateTime)
        );
        
        // 转换为DTO并填充最后一条消息
        List<ConversationDTO> conversationDTOs = conversationPage.getRecords().stream().map(conversation -> {
            ConversationDTO dto = new ConversationDTO();
            BeanUtils.copyProperties(conversation, dto);
            
            // 获取最后一条消息
            ConversationMessage lastMessage = conversationMessageMapper.selectOne(
                new LambdaQueryWrapper<ConversationMessage>()
                    .eq(ConversationMessage::getConversationId, conversation.getId())
                    .orderByDesc(ConversationMessage::getCreateTime)
                    .last("LIMIT 1")
            );
            
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", conversationDTOs);
        result.put("total", conversationPage.getTotal());
        
        return result;
    }
    
    @Override
    public List<MessageDTO> getMessages(String sessionId) {
        // 根据sessionId查询会话
        Conversation conversation = conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getSessionId, sessionId)
        );
        
        if (conversation == null) {
            return new ArrayList<>();
        }
        
        // 查询该会话的所有消息
        List<ConversationMessage> messages = conversationMessageMapper.selectList(
            new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getConversationId, conversation.getId())
                .orderByAsc(ConversationMessage::getCreateTime)
        );
        
        // 转换为DTO
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO();
            BeanUtils.copyProperties(message, dto);
            
            // 解析相关商品ID
            if (message.getRelatedProducts() != null && !message.getRelatedProducts().isEmpty()) {
                try {
                    List<Long> productIds = com.alibaba.fastjson2.JSON.parseArray(
                        message.getRelatedProducts(), Long.class);
                    dto.setRelatedProducts(productIds);
                } catch (Exception e) {
                    log.warn("解析相关商品ID失败: {}", message.getRelatedProducts());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void deleteConversation(String sessionId) {
        // 根据sessionId查询会话
        Conversation conversation = conversationMapper.selectOne(
            new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getSessionId, sessionId)
        );
        
        if (conversation == null) {
            log.warn("会话不存在: {}", sessionId);
            return;
        }
        
        // 删除会话的所有消息
        conversationMessageMapper.delete(
            new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getConversationId, conversation.getId())
        );
        
        // 删除会话
        conversationMapper.deleteById(conversation.getId());
        
        log.info("删除会话成功: sessionId={}", sessionId);
    }
}
