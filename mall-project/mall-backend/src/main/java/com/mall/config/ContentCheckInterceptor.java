package com.mall.config;

import com.alibaba.fastjson2.JSON;
import com.mall.common.Result;
import com.mall.service.AIContentCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 内容检测拦截器
 */
@Slf4j
@Component
public class ContentCheckInterceptor implements HandlerInterceptor {

    @Autowired
    private AIContentCheckService aiContentCheckService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 需要进行内容检测的路径
        String requestURI = request.getRequestURI();
        
        // 检查是否需要进行内容检测
        if (needContentCheck(requestURI)) {
            // 读取请求体
            String requestBody = readRequestBody(request);
            log.info("拦截到请求: {}, 请求体: {}", requestURI, requestBody);
            
            // 提取需要检测的内容
            String content = extractContent(requestBody, requestURI);
            if (content != null && !content.isEmpty()) {
                // 检测内容是否违规
                boolean isViolation = aiContentCheckService.isContentViolation(content);
                if (isViolation) {
                    // 返回违规提示
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JSON.toJSONString(Result.error("内容包含不当信息，请修改后重试")));
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 判断是否需要进行内容检测
     */
    private boolean needContentCheck(String requestURI) {
        // 智能助手聊天
        if (requestURI.contains("/assistant/chat")) {
            return true;
        }
        
        // 商品评论（如果有相关接口）
        if (requestURI.contains("/comment")) {
            return true;
        }
        
        // 管理员发布公告（如果有相关接口）
        if (requestURI.contains("/admin/announcement")) {
            return true;
        }
        
        return false;
    }

    /**
     * 读取请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 提取需要检测的内容
     */
    private String extractContent(String requestBody, String requestURI) {
        try {
            Map<String, Object> bodyMap = JSON.parseObject(requestBody, Map.class);
            
            // 智能助手聊天
            if (requestURI.contains("/assistant/chat")) {
                return (String) bodyMap.get("message");
            }
            
            // 商品评论
            if (requestURI.contains("/comment")) {
                return (String) bodyMap.get("content");
            }
            
            // 管理员发布公告
            if (requestURI.contains("/admin/announcement")) {
                return (String) bodyMap.get("content");
            }
        } catch (Exception e) {
            log.error("提取内容失败: {}", e.getMessage());
        }
        
        return null;
    }
}
