package com.mall.controller;

import com.mall.common.Result;
import com.mall.dto.ChatRequest;
import com.mall.dto.ChatResponse;
import com.mall.service.assistant.AssistantService;
import com.mall.service.assistant.impl.SimpleRAGServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 智能助手控制器
 */
@Slf4j
@RestController
@RequestMapping("/assistant")
public class AssistantController {
    
    @Autowired
    private AssistantService assistantService;

    @Autowired
    private SimpleRAGServiceImpl simpleRAGService;
    
    /**
     * 发送消息（普通请求）
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(
            HttpServletRequest request,
            @RequestBody ChatRequest chatRequest) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        log.info("用户 {} 发送消息: {}", userId, chatRequest.getMessage());
        ChatResponse response = assistantService.chat(userId, chatRequest);
        return Result.success(response);
    }
    
    /**
     * 流式发送消息（SSE）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            HttpServletRequest httpRequest,
            @RequestBody ChatRequest chatRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            userId = 1L; // 默认用户ID
        }
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        assistantService.chatStream(userId, chatRequest, new AssistantService.StreamCallback() {
            @Override
            public void onMessage(String chunk) {
                try {
                    // 直接发送data: 格式，与前端预期一致
                    emitter.send("data: " + chunk + "\n\n");
                } catch (IOException e) {
                    log.error("发送SSE消息失败", e);
                    emitter.completeWithError(e);
                }
            }
            
            @Override
            public void onComplete() {
                // 流式输出完成后，商品推荐已在保存消息时处理
                // 这里发送完成信号
                try {
                    // 直接发送data: [DONE] 格式，与前端预期一致
                    emitter.send("data: [DONE]\n\n");
                    emitter.complete();
                } catch (IOException e) {
                    log.error("完成SSE流失败", e);
                    emitter.completeWithError(e);
                }
            }
            
            @Override
            public void onError(Exception e) {
                log.error("流式对话出错", e);
                try {
                    // 发送错误信息给前端
                    emitter.send("data: " + e.getMessage() + "\n\n");
                } catch (IOException ex) {
                    log.error("发送错误信息失败", ex);
                }
                emitter.completeWithError(e);
            }
            
            @Override
            public void onProducts(java.util.List<com.mall.entity.Product> products) {
                // 发送商品推荐信息
                try {
                    java.util.Map<String, Object> productsData = new java.util.HashMap<>();
                    productsData.put("products", products);
                    // 直接发送data: 格式，与前端预期一致
                    emitter.send("data: " + com.alibaba.fastjson2.JSON.toJSONString(productsData) + "\n\n");
                } catch (IOException e) {
                    log.error("发送商品推荐失败", e);
                }
            }
        });
        
        return emitter;
    }
    
    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<?> getConversations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L;
        }
        java.util.Map<String, Object> result = assistantService.getConversations(userId, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取会话历史消息
     */
    @GetMapping("/conversation/{sessionId}/messages")
    public Result<?> getMessages(@PathVariable String sessionId) {
        java.util.List<com.mall.dto.MessageDTO> messages = assistantService.getMessages(sessionId);
        return Result.success(messages);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/conversation/{sessionId}")
    public Result<?> deleteConversation(@PathVariable String sessionId) {
        assistantService.deleteConversation(sessionId);
        return Result.success("删除成功");
    }

    /**
     * （扩展）一键同步商品向量到向量库（用于毕设演示环境）
     * 注意：生产环境建议做成管理端能力或定时任务，并加权限控制。
     */
    @PostMapping("/rag/sync/products")
    public Result<?> syncProductVectors() {
        simpleRAGService.init(); // 会根据配置决定是否自动同步
        return Result.success("已触发初始化/同步（如未开启向量检索则无动作）");
    }

    /**
     * （扩展）RAG调试接口：直接查看RAG检索到的商品
     * 便于本地调试关键词/向量检索效果
     */
    @GetMapping("/rag/debug/search")
    public Result<?> debugRagSearch(@RequestParam("q") String query,
                                    @RequestParam(value = "topK", defaultValue = "5") Integer topK) {
        java.util.List<com.mall.entity.Product> products = simpleRAGService.searchProducts(query, topK == null ? 5 : topK);
        return Result.success(products);
    }
}
