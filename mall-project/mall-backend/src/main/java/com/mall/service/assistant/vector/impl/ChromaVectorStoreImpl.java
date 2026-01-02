package com.mall.service.assistant.vector.impl;

import com.alibaba.fastjson2.JSON;
import com.mall.service.assistant.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Chroma向量库实现（HTTP REST）
 *
 * 说明：Chroma不同版本API会有差异，本实现尽量使用通用的v1接口：
 * - GET  /api/v1/collections
 * - POST /api/v1/collections  {name}
 * - POST /api/v1/collections/{collectionId}/upsert 或 /add
 * - POST /api/v1/collections/{collectionId}/query
 * - POST /api/v1/collections/{collectionId}/delete
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "vector-db.type", havingValue = "chroma", matchIfMissing = true)
public class ChromaVectorStoreImpl implements VectorStore {

    @Value("${vector-db.chroma.host:localhost}")
    private String host;

    @Value("${vector-db.chroma.port:8000}")
    private Integer port;

    @Value("${vector-db.chroma.collection-name:products}")
    private String collectionName;

    private volatile String collectionId;

    private String baseUrl() {
        return "http://" + host + ":" + port + "/api/v1";
    }

    @Override
    public void init() {
        try {
            ensureCollection();
            log.info("Chroma初始化完成: collectionName={}, collectionId={}", collectionName, collectionId);
        } catch (Exception e) {
            log.warn("Chroma初始化失败（将自动降级为关键词检索）: {}", e.getMessage());
        }
    }

    @Override
    public void upsert(List<String> ids, List<List<Float>> embeddings, List<String> documents, List<Map<String, Object>> metadatas) {
        ensureCollection();
        if (ids == null || ids.isEmpty()) return;

        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        body.put("embeddings", embeddings);
        if (documents != null) body.put("documents", documents);
        if (metadatas != null) body.put("metadatas", metadatas);

        // 优先尝试upsert，不支持则回退到add
        try {
            postJson("/collections/" + collectionId + "/upsert", body);
        } catch (RuntimeException ex) {
            log.warn("Chroma upsert失败，尝试add: {}", ex.getMessage());
            postJson("/collections/" + collectionId + "/add", body);
        }
    }

    @Override
    public List<String> query(List<Float> queryEmbedding, int topK) {
        ensureCollection();
        if (queryEmbedding == null || queryEmbedding.isEmpty()) return new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", Collections.singletonList(queryEmbedding));
        body.put("n_results", topK);
        body.put("include", Arrays.asList("distances"));

        Map<String, Object> res = postJson("/collections/" + collectionId + "/query", body);
        // 期望字段：ids: [[...]]
        Object idsObj = res.get("ids");
        if (!(idsObj instanceof List)) return new ArrayList<>();
        List<?> outer = (List<?>) idsObj;
        if (outer.isEmpty()) return new ArrayList<>();
        Object first = outer.get(0);
        if (!(first instanceof List)) return new ArrayList<>();
        List<?> inner = (List<?>) first;
        List<String> ids = new ArrayList<>();
        for (Object o : inner) {
            if (o != null) ids.add(String.valueOf(o));
        }
        return ids;
    }

    @Override
    public void delete(List<String> ids) {
        ensureCollection();
        if (ids == null || ids.isEmpty()) return;
        Map<String, Object> body = new HashMap<>();
        body.put("ids", ids);
        postJson("/collections/" + collectionId + "/delete", body);
    }

    private synchronized void ensureCollection() {
        if (collectionId != null && !collectionId.isEmpty()) return;

        // 1) list collections
        List<Map<String, Object>> collections = listCollections();
        for (Map<String, Object> c : collections) {
            if (c == null) continue;
            Object name = c.get("name");
            if (collectionName.equals(String.valueOf(name))) {
                collectionId = String.valueOf(c.get("id"));
                return;
            }
        }

        // 2) create collection
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", collectionName);
        Map<String, Object> created = postJson("/collections", createBody);
        Object id = created.get("id");
        if (id == null) {
            // 兼容部分版本创建后不直接返回id：再list一次
            collections = listCollections();
            for (Map<String, Object> c : collections) {
                if (c == null) continue;
                Object name = c.get("name");
                if (collectionName.equals(String.valueOf(name))) {
                    collectionId = String.valueOf(c.get("id"));
                    return;
                }
            }
            throw new RuntimeException("Chroma创建collection后未获取到collectionId");
        }
        collectionId = String.valueOf(id);
    }

    private List<Map<String, Object>> listCollections() {
        String url = baseUrl() + "/collections";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse resp = client.execute(get)) {
                String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                int code = resp.getStatusLine().getStatusCode();
                if (code >= 200 && code < 300) {
                    Object parsed = JSON.parse(body);
                    if (parsed instanceof List) {
                        // 避免泛型强转告警：逐个元素转换
                        List<?> raw = (List<?>) parsed;
                        List<Map<String, Object>> out = new ArrayList<>();
                        for (Object o : raw) {
                            if (!(o instanceof Map)) continue;
                            Map<?, ?> m = (Map<?, ?>) o;
                            Map<String, Object> mm = new HashMap<>();
                            for (Map.Entry<?, ?> e : m.entrySet()) {
                                mm.put(String.valueOf(e.getKey()), e.getValue());
                            }
                            out.add(mm);
                        }
                        return out;
                    }
                    return new ArrayList<>();
                }
                throw new RuntimeException("Chroma listCollections失败: http=" + code + ", body=" + body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Chroma listCollections异常: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> postJson(String path, Map<String, Object> body) {
        String url = baseUrl() + path;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));

            try (CloseableHttpResponse resp = client.execute(post)) {
                String respBody = resp.getEntity() == null ? "" : EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                int code = resp.getStatusLine().getStatusCode();
                if (code >= 200 && code < 300) {
                    if (respBody == null || respBody.trim().isEmpty()) {
                        return new HashMap<>();
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
                throw new RuntimeException("Chroma请求失败: path=" + path + ", http=" + code + ", body=" + respBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Chroma请求异常: " + e.getMessage(), e);
        }
    }
}


