package com.mall.service.assistant.impl;

import com.alibaba.fastjson2.JSON;
import com.mall.service.assistant.EmbeddingService;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * OpenAI兼容Embedding实现
 *
 * 兼容OpenAI/各类兼容OpenAI的服务（包括部分云厂商兼容模式）。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "embedding.provider", havingValue = "openai", matchIfMissing = false)
public class OpenAIEmbeddingServiceImpl implements EmbeddingService {

    @Value("${embedding.openai.api-key:}")
    private String apiKey;

    @Value("${embedding.openai.base-url:${llm.openai.base-url:https://api.openai.com/v1}}")
    private String baseUrl;

    @Value("${embedding.openai.model:text-embedding-3-small}")
    private String model;

    /**
     * 维度未知时返回0，不影响运行（仅用于展示）
     */
    @Value("${embedding.openai.dimension:0}")
    private Integer dimension;

    @Override
    public List<Float> embed(String text) {
        List<List<Float>> res = embedBatch(Collections.singletonList(text));
        return res.isEmpty() ? new ArrayList<>() : res.get(0);
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return new ArrayList<>();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("embedding.openai.api-key未配置");
        }
        String url = trimRightSlash(baseUrl) + "/embeddings";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", texts);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));

            try (CloseableHttpResponse resp = client.execute(post)) {
                String respBody = resp.getEntity() == null ? "" : EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                int code = resp.getStatusLine().getStatusCode();
                if (code < 200 || code >= 300) {
                    throw new RuntimeException("Embedding调用失败: http=" + code + ", body=" + respBody);
                }

                Map<?, ?> parsed = JSON.parseObject(respBody, Map.class);
                Object dataObj = parsed.get("data");
                if (!(dataObj instanceof List)) return new ArrayList<>();

                List<?> data = (List<?>) dataObj;
                List<List<Float>> vectors = new ArrayList<>();
                for (Object item : data) {
                    if (!(item instanceof Map)) continue;
                    Map<?, ?> m = (Map<?, ?>) item;
                    Object embObj = m.get("embedding");
                    if (!(embObj instanceof List)) continue;
                    List<?> arr = (List<?>) embObj;
                    List<Float> v = new ArrayList<>(arr.size());
                    for (Object n : arr) {
                        if (n instanceof Number) v.add(((Number) n).floatValue());
                    }
                    vectors.add(v);
                }
                return vectors;
            }
        } catch (Exception e) {
            throw new RuntimeException("Embedding调用异常: " + e.getMessage(), e);
        }
    }

    @Override
    public int getDimension() {
        return dimension == null ? 0 : dimension;
    }

    private String trimRightSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}


