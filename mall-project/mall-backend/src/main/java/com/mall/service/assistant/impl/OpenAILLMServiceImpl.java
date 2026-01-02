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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * OpenAI兼容对话实现（支持stream=true）
 *
 * 可对接OpenAI/兼容OpenAI协议的第三方（含部分云厂商兼容模式）。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAILLMServiceImpl implements LLMService {

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.openai.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public String chat(String prompt, List<Map<String, String>> history) {
        Map<String, Object> body = buildBody(prompt, history, false);
        Map<String, Object> resp = postJson("/chat/completions", body);
        Object choicesObj = resp.get("choices");
        if (!(choicesObj instanceof List)) return "抱歉，我暂时无法回答您的问题。";
        List<?> choices = (List<?>) choicesObj;
        if (choices.isEmpty()) return "抱歉，我暂时无法回答您的问题。";
        Object first = choices.get(0);
        if (!(first instanceof Map)) return "抱歉，我暂时无法回答您的问题。";
        Map<?, ?> firstMap = (Map<?, ?>) first;
        Object msgObj = firstMap.get("message");
        if (!(msgObj instanceof Map)) return "抱歉，我暂时无法回答您的问题。";
        Map<?, ?> msg = (Map<?, ?>) msgObj;
        Object content = msg.get("content");
        return content == null ? "抱歉，我暂时无法回答您的问题。" : String.valueOf(content);
    }

    @Override
    public void chatStream(String prompt, List<Map<String, String>> history, StreamCallback callback) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError(new RuntimeException("llm.openai.api-key未配置"));
            return;
        }

        new Thread(() -> {
            String url = trimRightSlash(baseUrl) + "/chat/completions";
            Map<String, Object> body = buildBody(prompt, history, true);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(url);
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + apiKey);
                post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));

                try (CloseableHttpResponse resp = client.execute(post)) {
                    int code = resp.getStatusLine().getStatusCode();
                    if (code < 200 || code >= 300) {
                        String err = resp.getEntity() == null ? "" : EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                        callback.onError(new RuntimeException("OpenAI流式调用失败: http=" + code + ", body=" + err));
                        return;
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) continue;
                            if (!line.startsWith("data:")) continue;

                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                callback.onComplete();
                                return;
                            }

                            Map<?, ?> event = JSON.parseObject(data, Map.class);
                            Object choicesObj = event.get("choices");
                            if (!(choicesObj instanceof List)) continue;
                            List<?> choices = (List<?>) choicesObj;
                            if (choices.isEmpty()) continue;
                            Object c0 = choices.get(0);
                            if (!(c0 instanceof Map)) continue;
                            Map<?, ?> c0m = (Map<?, ?>) c0;
                            Object deltaObj = c0m.get("delta");
                            if (!(deltaObj instanceof Map)) continue;
                            Map<?, ?> delta = (Map<?, ?>) deltaObj;
                            Object contentObj = delta.get("content");
                            if (contentObj != null) {
                                callback.onMessage(String.valueOf(contentObj));
                            }
                        }
                        callback.onComplete();
                    }
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private Map<String, Object> buildBody(String prompt, List<Map<String, String>> history, boolean stream) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (history != null) {
            for (Map<String, String> h : history) {
                if (h == null) continue;
                String role = h.get("role");
                String content = h.get("content");
                if (role == null || content == null) continue;
                Map<String, String> m = new HashMap<>();
                m.put("role", role);
                m.put("content", content);
                messages.add(m);
            }
        }
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", prompt);
        messages.add(user);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", stream);
        return body;
    }

    private Map<String, Object> postJson(String path, Map<String, Object> body) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("llm.openai.api-key未配置");
        }
        String url = trimRightSlash(baseUrl) + path;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));

            try (CloseableHttpResponse resp = client.execute(post)) {
                String respBody = resp.getEntity() == null ? "" : EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                int code = resp.getStatusLine().getStatusCode();
                if (code < 200 || code >= 300) {
                    throw new RuntimeException("OpenAI调用失败: http=" + code + ", body=" + respBody);
                }
                Map<?, ?> raw = JSON.parseObject(respBody, Map.class);
                Map<String, Object> out = new HashMap<>();
                if (raw != null) {
                    for (Map.Entry<?, ?> e : raw.entrySet()) {
                        out.put(String.valueOf(e.getKey()), e.getValue());
                    }
                }
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenAI调用异常: " + e.getMessage(), e);
        }
    }

    private String trimRightSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}


