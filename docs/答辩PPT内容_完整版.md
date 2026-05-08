# 商城智能助手系统 - 答辩PPT（完整版）

---

## 第1页：封面

**商城智能助手系统**

> 基于检索增强生成(RAG)与大语言模型的智能购物助手

**姓名**：XXX  
**学号**：XXX  
**专业**：XXX  
**指导教师**：XXX  
**答辩日期**：2026年5月

---

## 第2页：目录

1. 项目概述与建设目标
2. 技术架构与技术栈
3. 用户端功能模块详解
4. 管理端功能模块详解
5. 智能助手子系统（核心重点）
   - 架构位置与数据流
   - 数据模型设计
   - 完整时序流程
   - RAG商品检索机制
   - RAG订单检索机制
   - 提示词工程设计
   - 通义千问流式调用
   - SSE与语义单元切分
   - 内容安全检测
   - 管理端策略控制
   - API接口契约
6. 数据库与核心实体设计
7. 代码结构与关键类解析
8. 创新点与技术亮点
9. 演示方案与答辩话术
10. 不足与改进方向
11. 总结与致谢

---

## 第3页：项目概述

### 系统名称与定位
- **系统名称**：商城智能助手系统（`mall-backend`、`mall-frontend`）
- **系统定位**：B/S架构、前后端分离的电商业务系统
- **差异化核心**：在真实商城数据之上实现对话式智能助手

### 建设目标

| 维度 | 目标 | 实现方式 |
|------|------|----------|
| **业务** | 完整网购闭环与管理闭环 | 用户端+管理端全功能 |
| **智能** | 回答绑定商品与订单事实，控制幻觉 | RAG检索增强 + 提示词约束 |
| **体验** | 流式打字效果、会话历史、商品推荐卡片 | SSE + 语义单元切分 |
| **治理** | 入站内容检测、一键暂停、模板模式 | ContentCheckInterceptor + 策略开关 |
| **工程** | 分层清晰、接口化LLM/RAG | Spring Boot分层架构 |

### 工程结构
```
mall-project/
├── mall-backend/        # Spring Boot 单体应用
│   └── src/main/java/com/mall/
│       ├── controller/   # REST API
│       ├── service/      # 业务逻辑（含assistant子模块）
│       ├── mapper/       # 数据访问
│       ├── entity/       # 实体类
│       └── config/       # 配置类
└── mall-frontend/       # Vue3 + Vite SPA
    └── src/
        ├── views/        # 页面视图
        ├── components/   # 组件（含assistant子模块）
        ├── api/          # API封装
        └── store/        # 状态管理
```

---

## 第4页：技术架构

### 逻辑三层架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      表现层 (Presentation)                      │
│  Vue3 + Router + Pinia + Element Plus + Vite                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 页面路由 │ 助手UI │ HTTP/SSE消费 │ 状态管理 │              │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                      应用层 (Application)                       │
│              Spring Boot 2.7.18                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Controller │ Service │ Mapper │ JWT │ RAG │ LLM HTTP   │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                        数据层 (Data)                            │
│  ┌─────────────────┐  ┌──────────┐  ┌─────────────────┐       │
│  │ MySQL + Druid   │  │  Redis   │  │ Chroma/Milvus   │       │
│  │ MyBatis-Plus    │  │ (缓存)   │  │ (向量库)        │       │
│  └─────────────────┘  └──────────┘  └─────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### 运行与网络配置

| 组件 | 端口 | 配置说明 |
|------|------|----------|
| 前端开发 | 3000 | vite.config.ts代理/api到后端 |
| 后端服务 | 8087 | server.port=8087, context-path=/api |
| 大模型 | - | 通义千问 qwen-turbo HTTP API |
| MySQL | 3306 | 业务数据持久化 |
| Redis | 6379 | 缓存与会话 |

---

## 第5页：技术栈摘要

### 后端技术栈

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Spring Boot | 2.7.18 | 应用容器 |
| ORM | MyBatis-Plus | 3.5.3.1 | 数据访问 |
| 数据库 | MySQL | 8.0+ | 业务数据 |
| 缓存 | Redis | 6.0+ | 缓存 |
| 连接池 | Druid | 1.2.16 | 数据库连接 |
| 安全 | JWT (jjwt) | 0.11.5 | 身份认证 |
| 密码加密 | Spring Security Crypto | 5.7.x | BCrypt |
| JSON | FastJSON2 | 2.0.40 | JSON处理 |
| HTTP客户端 | Apache HttpClient | 4.5.x | 大模型调用 |
| AI服务 | 通义千问API | qwen-turbo | 大语言模型 |

### 前端技术栈

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Vue | 3.3+ | UI框架 |
| 语言 | TypeScript | 5.0+ | 类型安全 |
| 构建 | Vite | 5.0+ | 构建工具 |
| 路由 | Vue Router | 4.0+ | 页面路由 |
| 状态管理 | Pinia | 2.0+ | 状态管理 |
| UI组件 | Element Plus | 2.4+ | UI组件库 |
| HTTP | Axios | 1.0+ | HTTP请求 |
| 流式处理 | fetch + ReadableStream | - | SSE消费 |

---

## 第6页：用户端功能模块详解

### 全局鉴权机制
- **无token跳转**：除`/login`、`/register`外，其他页面`requiresAuth: true`
- **Token存储**：`localStorage.token`
- **请求头封装**：`Authorization: Bearer <token>`（`src/api/request.ts`统一封装）

### 订单状态约定
| 状态码 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 待支付 | 订单已创建，等待支付 |
| 1 | 已支付 | 支付成功 |
| 2 | 已发货 | 商家已发货 |
| 3 | 已完成 | 用户确认收货 |
| 4 | 已取消 | 订单取消 |

### 核心功能模块

| 页面 | 路由 | 核心接口 | 功能说明 |
|------|------|----------|----------|
| 登录 | /login | POST /user/login | 表单校验、token存储、跳转首页 |
| 注册 | /register | POST /user/register | 用户名3-20字符、密码6-30字符 |
| 首页 | /home | GET /products/hot/new | Hero主视觉、热门/最新商品 |
| 商品列表 | /products | GET /products/search | 关键词搜索、分页、快速加购 |
| 商品详情 | /products/:id | GET /products/{id} | 展示、评价、收藏、加购 |
| 购物车 | /cart | GET/POST/PUT/DELETE /cart | 数量修改、批量删除、结算 |
| 订单列表 | /orders | GET /order/list | 状态Tab、支付、取消 |
| 地址管理 | /address | GET/POST/PUT/DELETE /address | 增删改查、设为默认 |
| 个人中心 | /profile | GET /user/info | 信息修改、密码修改 |
| 收藏 | /favorites | GET/DELETE /favorites | 收藏列表、取消收藏 |
| 智能助手 | /assistant | POST /assistant/chat | 会话、聊天、商品推荐 |

---

## 第7页：管理端功能模块详解

### 管理员鉴权
- **独立登录**：`POST /user/admin/login`（与普通用户登录分离）
- **Token存储**：同用户端，写入`localStorage`
- **路由前缀**：`/admin`

### 核心功能模块

| 页面 | 路由 | 核心接口 | 功能说明 |
|------|------|----------|----------|
| 管理员登录 | /admin/login | POST /user/admin/login | 管理员专属登录 |
| 仪表盘 | /admin/dashboard | GET /admin/dashboard | 商品/订单/用户统计、最近订单、热销商品 |
| 商品列表 | /admin/products | GET /products/admin/list | 检索、上下架开关、删除 |
| 添加商品 | /admin/products/add | POST /products | 名称、分类、价格、库存、图片上传、AI生成描述 |
| 订单列表 | /admin/orders | GET /admin/orders | 筛选、详情、发货、取消 |
| 订单分析 | /admin/orders/analysis | GET/POST /admin/orders/analysis | 统计数据、图表、AI分析 |
| 用户列表 | /admin/users | GET /admin/users | 筛选、状态开关、角色设置 |
| 智能助手管理 | /admin/assistant | GET/PUT /admin/assistant/settings | 设置开关、响应模式、模板管理 |

### AI增强功能
- **商品描述生成**：`POST /products/generate-content`
- **订单AI分析**：`POST /admin/orders/analysis/ai`

---

## 第8页：智能助手子系统 - 架构位置

### 完整数据流图

```
[用户浏览器]
    │
    ├─ JSON ────────────────────────────┐
    │                                   │
    └─ SSE(fetch ReadableStream) ───────┼──► [AssistantController]
                                        │       │
                                        │       ▼
                                        │  [ContentCheckInterceptor]
                                        │       │ (入站内容检测)
                                        │       ▼
                                        │  [AssistantServiceImpl]
                                        │       │
                                        │       ├──► [ConversationMapper]
                                        │       │       [ConversationMessageMapper]
                                        │       │
                                        │       ├──► [AssistantSettingsMapper]
                                        │       │       [AssistantTemplateMapper]
                                        │       │
                                        │       ├──► [RAGService]
                                        │       │       ├── [ProductRAGDataSource]
                                        │       │       └── [OrderRAGDataSource]
                                        │       │               │
                                        │       │               ▼
                                        │       │       [EmbeddingService] ──► [VectorStore]
                                        │       │
                                        │       └──► [LLMService]
                                        │               │
                                        │               ▼
                                        └──────────► [通义千问API]
```

### 智能助手在系统中的位置
- **入口**：用户端导航栏 `/assistant` 路由
- **核心能力**：对话式商品推荐、订单查询、购物引导
- **技术支撑**：RAG检索 + LLM生成 + SSE流式响应

---

## 第9页：智能助手子系统 - 数据模型设计

### 核心数据表

| 表名 | 实体类 | 核心字段 | 用途 |
|------|--------|----------|------|
| `conversation` | `Conversation` | userId, sessionId, title, createdAt | 会话主表，sessionId为UUID |
| `conversation_message` | `ConversationMessage` | conversationId, role(1=用户/2=助手), content, relatedProducts(JSON) | 消息详情，relatedProducts存关联商品ID |
| `assistant_settings` | `AssistantSettings` | name, enabled(0/1), responseMode(intelligent/template), timeout | 全局策略配置 |
| `assistant_template` | `AssistantTemplate` | keyword, response, enabled | 模板回复规则 |

### 会话标题生成逻辑
```
首条用户消息写入后：
IF title == "新对话" THEN
    title = SUBSTRING(userMessage, 0, 14)
END IF
```

### 消息时序存储
```
conversation_message表：
- role=1: 用户消息
- role=2: 助手消息
- relatedProducts: JSON数组，如[1, 3, 5]表示关联商品ID列表
```

---

## 第10页：智能助手子系统 - 完整时序流程

### 单次对话后端编排流程

```
用户发送消息
    │
    ▼
1. 会话解析
   ├─ 有有效sessionId且存在 → 复用会话
   └─ 无 → 新建会话（生成新sessionId）
    │
    ▼
2. 落库用户消息
   └─ conversation_message(role=1, content=用户消息)
    │
    ▼
3. 管理端策略短路 [resolveManagedReply]
   ├─ enabled=0 → 返回"助手已暂停"，不调用RAG/LLM
   ├─ responseMode=template → 遍历模板，命中则返回固定回复
   └─ 未命中模板 → 继续智能链路
    │
    ▼
4. RAG检索商品
   └─ ragService.searchProducts(message, 5) → Top 5商品
    │
    ▼
5. RAG检索订单
   └─ ragService.searchUserOrders(userId, message, 5) → 意图触发时检索
    │
    ▼
6. 构建系统提示词
   └─ buildPrompt(userMessage, products, orders)
    │
    ▼
7. 组装多轮历史
   └─ getHistoryMessages(conversationId, 10) → 最近10条
    │
    ▼
8. 调用LLM
   ├─ 非流式：llmService.chat(prompt, history)
   └─ 流式：llmService.chatStream(prompt, history, callback)
    │
    ▼
9. 落库助手消息
   └─ conversation_message(role=2, content=回复, relatedProducts=商品ID数组)
    │
    ▼
10. 返回响应
    └─ ChatResponse(sessionId, message, relatedProducts, messageId)
```

---

## 第11页：智能助手子系统 - RAG商品检索机制

### 检索决策链

```
用户查询
    │
    ▼
配置检查：rag.vectorEnabled && rag.searchMode==vector
    │
    ├─ YES → 向量检索 [vectorSearchProducts]
    │           │
    │           ├─ 查询embedding → 向量库query → 获取商品ID
    │           ├─ productMapper.selectBatchIds(ids)
    │           └─ 过滤status=1(上架)的商品
    │                 │
    │                 ├─ 有结果 → 返回
    │                 └─ 无结果 → 回退关键词检索
    │
    └─ NO → 关键词检索 [ProductRAGDataSource]
```

### 关键词打分规则

```java
// scoreProduct 核心逻辑
int score = 0;
String[] keywords = query.toLowerCase().split("[ ,，]");

for (String keyword : keywords) {
    if (product.getName().contains(keyword)) score += 5;
    if (product.getDescription().contains(keyword)) score += 3;
    if (product.getSpecsJson().contains(keyword)) score += 2;
}

// 价格加权
if (score > 0 && product.getPrice() != null) {
    if (query.contains("便宜") || query.contains("入门")) {
        score += (int)(100 / product.getPrice()) * 2;
    }
    if (query.contains("高端") || query.contains("旗舰")) {
        score += (int)(product.getPrice() / 100);
    }
}

return score;
```

---

## 第12页：智能助手子系统 - RAG订单检索机制

### 安全前提检查

```java
// OrderRAGDataSource 安全校验
if (filters.getUserId() == null || filters.getUserId() <= 0) {
    return Collections.emptyList();  // 无userId直接返回空，防止跨用户泄露
}
```

### 意图触发判定

```java
// shouldRetrieveOrders 判断逻辑
boolean shouldRetrieve = false;

// 规则1：匹配订单号格式
if (message.matches(".*\\b(ORD\\d+)\\b.*")) {
    shouldRetrieve = true;
}

// 规则2：包含订单相关关键词
String[] orderKeywords = {"我的订单", "订单状态", "物流", "快递", "发货", "到哪了"};
for (String keyword : orderKeywords) {
    if (message.contains(keyword)) {
        shouldRetrieve = true;
        break;
    }
}

// 规则3：排除纯流程咨询
String[] excludeKeywords = {"如何下单", "下单流程"};
for (String keyword : excludeKeywords) {
    if (message.contains(keyword)) {
        shouldRetrieve = false;
        break;
    }
}

return shouldRetrieve;
```

### 订单结构化字段

| 字段 | 说明 | 示例 |
|------|------|------|
| orderNo | 订单号 | ORD202401010001 |
| amount | 金额 | 299.00 |
| status | 状态 | 已完成 |
| statusText | 状态文案 | 您的订单已完成 |
| createTime | 创建时间 | 2024-01-01 10:30:00 |
| itemsSummary | 明细摘要 | 无线键盘 x1 |
| logisticsSummary | 履约说明 | 订单已完成，请查收商品 |

---

## 第13页：智能助手子系统 - 提示词工程设计

### 提示词结构（防幻觉关键设计）

```
【角色定义】
你是一个专业的商城智能购物助手，你的任务是：
1. 仅回答与本商城购物相关的问题
2. 对于与商城无关的问题，礼貌拒绝并说明只能回答商城相关问题
3. 回答必须基于提供的检索结果，不得编造信息

【订单系统检索结果】（当前登录用户，来自数据库）
---
订单号：ORD202401010001
金额：¥299.00
状态：已完成
创建时间：2024-01-01 10:30:00
支付时间：2024-01-01 10:35:00
发货时间：2024-01-02 09:00:00
完成时间：2024-01-05 14:00:00
商品明细：无线键盘 x1
履约说明：订单已完成，请查收商品
---

【商品检索结果】（最多5条）
---
1. 无线键盘 - ¥99 - 库存100 - 简约设计，静音按键
2. 机械键盘 - ¥199 - 库存50 - RGB背光，青轴
3. 蓝牙键盘 - ¥129 - 库存80 - 多设备连接
---

【用户当前问题】
{用户消息}

【回答规范】
1. 使用简体中文，友好专业，分点分段
2. 推荐商品时必须引用列表中的商品名称并结合价格说明理由
3. 订单状态、物流信息必须严格依据【订单系统检索结果】中的字段
4. 若无相关数据，引导用户查看"我的订单"或重新登录
5. 输出格式：先「思路：」2-4句说明思考过程，再「回答：」给出完整建议
6. 禁止编造任何未在检索结果中出现的商品或订单信息
```

### 提示词设计原则
1. **角色明确**：限定助手边界，拒答无关问题
2. **证据块隔离**：明确标注数据来源
3. **硬约束规则**：强制基于证据回答
4. **输出格式规范**：结构化输出便于解析

---

## 第14页：智能助手子系统 - 通义千问流式调用

### 非流式调用

```java
// QwenLLMServiceImpl.chat()
public String chat(String prompt, List<Message> history) {
    List<Map<String, Object>> messages = new ArrayList<>();
    
    // 添加历史消息
    for (Message msg : history) {
        messages.add(Map.of(
            "role", msg.getRole(),  // "user" 或 "assistant"
            "content", msg.getContent()
        ));
    }
    
    // 添加当前prompt作为user消息
    messages.add(Map.of(
        "role", "user",
        "content", prompt
    ));
    
    // 构建请求体
    Map<String, Object> requestBody = Map.of(
        "model", "qwen-turbo",
        "input", Map.of("messages", messages),
        "parameters", Map.of(
            "temperature", 0.7,
            "max_tokens", 2048
        )
    );
    
    // 调用API
    String response = httpClient.post(qwenBaseUrl, requestBody);
    return parseResponse(response);  // 解析 output.text
}
```

### 流式调用

```java
// QwenLLMServiceImpl.chatStream()
public void chatStream(String prompt, List<Message> history, StreamCallback callback) {
    Map<String, Object> requestBody = Map.of(
        "model", "qwen-turbo",
        "input", Map.of("messages", messages),
        "parameters", Map.of("stream", true),  // 关键：开启流式
        "stream", true
    );
    
    // 流式响应处理
    httpClient.postStream(qwenBaseUrl, requestBody, (chunk) -> {
        String dataLine = chunk.trim();
        if (dataLine.startsWith("data: ")) {
            String jsonStr = dataLine.substring(6);
            if (!jsonStr.equals("[DONE]")) {
                // 解析增量内容
                JSONObject json = JSON.parseObject(jsonStr);
                String fullText = json.getJSONObject("output").getString("text");
                
                // 计算增量
                String delta = fullText.substring(lastLength);
                lastLength = fullText.length();
                
                // 回调推送
                callback.onMessage(delta);
            }
        }
    });
}
```

---

## 第15页：智能助手子系统 - SSE与语义单元切分

### 后端语义单元切分

```java
// streamBySemanticUnits 核心逻辑
private void streamBySemanticUnits(String text, Consumer<String> callback) {
    StringBuilder wordBuffer = new StringBuilder();
    
    for (char c : text.toCharArray()) {
        if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
            // 英文单词：攒成完整词再推送
            wordBuffer.append(c);
        } else {
            // 非ASCII字符：先推送已攒的英文词
            if (wordBuffer.length() > 0) {
                callback.accept(wordBuffer.toString());
                wordBuffer = new StringBuilder();
                Thread.sleep(randomDelay(60, 100));
            }
            // 中日文等：单字符推送
            callback.accept(String.valueOf(c));
            Thread.sleep(randomDelay(60, 100));
        }
    }
    
    // 推送剩余的英文词
    if (wordBuffer.length() > 0) {
        callback.accept(wordBuffer.toString());
    }
}
```

### SSE控制器配置

```java
// AssistantController.chatStream()
@PostMapping("/chat/stream")
public SseEmitter chatStream(@RequestBody ChatRequest request) {
    SseEmitter emitter = new SseEmitter(60000L);  // 60秒超时
    
    // 响应头配置：禁用缓存
    emitter.setHeader("Cache-Control", "no-cache");
    emitter.setHeader("Connection", "keep-alive");
    emitter.setHeader("X-Accel-Buffering", "no");
    
    // 异步处理
    executorService.submit(() -> {
        assistantService.chatStream(request, new StreamCallback() {
            @Override
            public void onMessage(String chunk) {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunk));
            }
            
            @Override
            public void onProducts(List<Product> products) {
                emitter.send(SseEmitter.event()
                    .name("products")
                    .data(objectMapper.writeValueAsString(products)));
            }
            
            @Override
            public void onComplete() {
                emitter.complete();
            }
        });
    });
    
    return emitter;
}
```

### 前端流式消费

```typescript
// src/api/assistant.ts - chatStreamPost
async function chatStreamPost(request: ChatRequest): Promise<void> {
    const response = await fetch('/api/assistant/chat/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
    });
    
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    
    while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        
        const lines = decoder.decode(value).split('\n');
        for (const line of lines) {
            if (line.startsWith('data: ')) {
                const data = line.substring(6);
                try {
                    const json = JSON.parse(data);
                    if (json.products) {
                        // 处理商品推荐
                        handleProducts(json.products);
                    }
                } catch {
                    // 纯文本消息
                    appendMessage(data);
                }
            }
        }
    }
}
```

---

## 第16页：智能助手子系统 - 内容安全检测

### 检测拦截流程

```
用户消息 → /assistant/chat
                │
                ▼
    ContentCheckInterceptor.preHandle()
                │
                ├─ 读取请求体
                │       │
                │       ▼
                │  解析 message/content 字段
                │       │
                │       ▼
                │  AIContentCheckService.isContentViolation()
                │       │
                │       ▼
                │  构造检测prompt → LLM调用
                │       │
                │       ▼
                │  解析返回JSON：{isViolation, violationType}
                │       │
                ├─ 违规 → Result.error("内容违规") → 拦截
                │
                └─ 合规 → 继续助手主逻辑
```

### 检测Prompt示例

```
请检测以下文本是否包含违规内容：

【待检测文本】
{用户消息}

【检测要求】
1. 判断是否违规：是/否
2. 违规类型（多选）：色情、暴力、政治敏感、广告推广、恶意攻击、其他
3. 违规理由

【输出格式】
{"isViolation": true/false, "violationType": ["类型1", "类型2"], "reason": "理由"}
```

### 拦截路径配置

| 路径 | 检测字段 | 说明 |
|------|----------|------|
| /assistant/chat | message | 助手聊天消息 |
| /assistant/chat/stream | message | 流式聊天消息 |
| /comment | content | 用户评论（预留） |
| /admin/announcement | content | 管理员公告（预留） |

---

## 第17页：智能助手子系统 - 管理端策略控制

### 三种响应模式对比

| 模式 | enabled | responseMode | 行为 | 适用场景 |
|------|---------|--------------|------|----------|
| **停用** | 0 | - | 返回固定话术"助手已暂停"，不调用RAG/LLM | 维护、紧急情况、演示 |
| **模板模式** | 1 | template | 遍历模板，命中返回固定回复；未命中回退智能 | 常见问题快速响应、成本控制 |
| **智能模式** | 1 | intelligent | 完整RAG检索 + LLM生成 | 正常业务 |

### 模板匹配逻辑

```java
// resolveManagedReply 模板匹配
public String resolveManagedReply(String userMessage) {
    // 检查是否启用
    AssistantSettings settings = assistantSettingsMapper.selectLatest();
    if (settings.getEnabled() == 0) {
        return "当前智能助手已暂停服务，请稍后再试。";
    }
    
    // 模板模式检查
    if ("template".equals(settings.getResponseMode())) {
        List<AssistantTemplate> templates = assistantTemplateMapper.selectList(null);
        String trimmedMessage = userMessage.trim();
        
        for (AssistantTemplate template : templates) {
            if (trimmedMessage.contains(template.getKeyword().trim())) {
                return template.getResponse();  // 命中模板，返回固定回复
            }
        }
        
        // 未命中模板，返回null让流程继续到智能链路
        return null;
    }
    
    return null;  // 智能模式，继续正常流程
}
```

### 管理端设置API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /admin/assistant/settings | 获取当前设置（无记录时返回默认值） |
| PUT | /admin/assistant/settings | 更新设置 |
| GET | /admin/assistant/templates | 获取模板列表 |
| POST | /admin/assistant/templates | 新增模板 |
| PUT | /admin/assistant/templates/{id} | 更新模板 |
| DELETE | /admin/assistant/templates/{id} | 删除模板 |

---

## 第18页：智能助手子系统 - API接口契约

### 客户端接口

| 方法 | 路径 | 请求体 | 响应体 | 说明 |
|------|------|--------|--------|------|
| POST | /assistant/chat | `{sessionId?, message}` | `{sessionId, message, relatedProducts, messageId}` | 非流式聊天 |
| POST | /assistant/chat/stream | `{sessionId?, message}` | SSE流 | 流式聊天 |
| GET | /assistant/conversations?page&size | - | `{list, total}` | 会话列表 |
| GET | /assistant/conversation/{sessionId}/messages | - | `[{role, content, relatedProducts, createdAt}]` | 会话消息 |
| DELETE | /assistant/conversation/{sessionId} | - | Result | 删除会话 |

### 请求/响应示例

**POST /assistant/chat**

请求：
```json
{
  "sessionId": "abc123",
  "message": "推荐一款便宜的键盘"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "abc123",
    "message": "思路：用户想要便宜的键盘，我需要查看商品检索结果中的低价键盘...\n\n回答：根据您的需求，为您推荐以下高性价比键盘：\n1. 无线键盘 - ¥99，简约设计静音按键，库存充足\n2. 蓝牙键盘 - ¥129，支持多设备连接",
    "relatedProducts": [1, 3],
    "messageId": 1001
  }
}
```

### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /admin/assistant/settings | 获取助手设置 |
| PUT | /admin/assistant/settings | 更新助手设置 |
| GET | /admin/assistant/templates | 获取模板列表 |
| POST | /admin/assistant/templates | 新增模板 |
| PUT | /admin/assistant/templates/{id} | 更新模板 |
| DELETE | /admin/assistant/templates/{id} | 删除模板 |

---

## 第19页：数据库与核心实体设计

### 核心实体关系图

```
User 1 ─── * Conversation 1 ─── * ConversationMessage
            │
            └── userId (外键)

AssistantSettings (单例)
AssistantTemplate (*)
```

### 实体字段详解

**Conversation（会话）**
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 主键 |
| userId | Long | NOT NULL | 用户ID |
| sessionId | String | UNIQUE | UUID会话标识 |
| title | String | DEFAULT '新对话' | 会话标题 |
| createdAt | DateTime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updatedAt | DateTime | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**ConversationMessage（消息）**
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 主键 |
| conversationId | Long | FK → Conversation.id | 会话ID |
| role | Integer | NOT NULL | 1=用户, 2=助手 |
| content | Text | NOT NULL | 消息内容 |
| relatedProducts | Text | NULL | JSON数组，商品ID列表 |
| createdAt | DateTime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**AssistantSettings（设置）**
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK | 主键（通常只有1条记录） |
| name | String | NOT NULL | 助手名称 |
| enabled | Integer | DEFAULT 1 | 0=停用, 1=启用 |
| responseMode | String | DEFAULT 'intelligent' | intelligent/template |
| timeout | Integer | DEFAULT 30 | 超时秒数 |
| updatedAt | DateTime | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

---

## 第20页：代码结构与关键类解析

### 后端代码结构

```
com.mall/
├── controller/
│   ├── AssistantController.java      # REST + SSE出口
│   ├── AdminAssistantController.java # 管理端配置接口
│   └── ...其他Controller
├── service/
│   ├── assistant/
│   │   ├── AssistantService.java     # 助手服务接口
│   │   ├── AssistantServiceImpl.java # 核心编排逻辑
│   │   ├── RAGService.java           # RAG服务接口
│   │   ├── SimpleRAGServiceImpl.java # RAG实现
│   │   ├── LLMService.java           # LLM服务接口
│   │   ├── QwenLLMServiceImpl.java   # 通义千问实现
│   │   ├── RAGDataSource.java        # 数据源接口
│   │   ├── ProductRAGDataSource.java # 商品检索
│   │   ├── OrderRAGDataSource.java   # 订单检索
│   │   ├── EmbeddingService.java     # 向量化接口
│   │   └── VectorStore.java          # 向量库接口
│   ├── AIContentCheckService.java    # 内容检测服务
│   └── ...其他Service
├── mapper/
│   ├── ConversationMapper.java
│   ├── ConversationMessageMapper.java
│   ├── AssistantSettingsMapper.java
│   ├── AssistantTemplateMapper.java
│   └── ...其他Mapper
├── entity/
│   ├── Conversation.java
│   ├── ConversationMessage.java
│   ├── AssistantSettings.java
│   ├── AssistantTemplate.java
│   └── ...其他Entity
├── config/
│   ├── RAGConfig.java                # RAG配置
│   ├── LLMConfig.java                # LLM配置
│   ├── ContentCheckInterceptor.java  # 内容检测拦截器
│   ├── JwtInterceptor.java           # JWT拦截器
│   └── ...其他Config
└── MallApplication.java              # 入口类
```

### 前端代码结构

```
src/
├── views/
│   ├── Assistant.vue                 # 助手主页面
│   └── ...其他视图
├── components/
│   └── assistant/
│       ├── ChatWindow.vue            # 聊天窗口
│       ├── ConversationList.vue      # 会话列表
│       ├── MessageInput.vue          # 消息输入
│       └── ProductRecommendation.vue # 商品推荐卡片
├── api/
│   ├── assistant.ts                  # 助手API封装
│   └── ...其他API
├── store/
│   └── user.ts                       # 用户状态
├── router/
│   └── index.ts                      # 路由配置
└── main.ts                           # 入口文件
```

### 关键类职责说明

| 类 | 职责 | 核心方法 |
|----|------|----------|
| AssistantController | REST/SSE入口 | chat(), chatStream() |
| AssistantServiceImpl | 会话编排 | chat(), chatStream(), buildPrompt(), resolveManagedReply() |
| SimpleRAGServiceImpl | RAG路由 | searchProducts(), searchUserOrders(), syncAllProductsToVectorStore() |
| ProductRAGDataSource | 商品检索 | search(), scoreProduct() |
| OrderRAGDataSource | 订单检索 | search(), shouldRetrieveOrders() |
| QwenLLMServiceImpl | LLM调用 | chat(), chatStream() |
| ContentCheckInterceptor | 内容检测 | preHandle() |

---

## 第21页：创新点与技术亮点

### 核心创新点

#### 1. 业务数据驱动的对话式导购
- **RAG双数据源**：同时覆盖商品库与用户订单子集
- **事实锚点**：提示词强制"有证据才答单"
- **幻觉控制**：订单状态严格依据检索结果

#### 2. 可运营、可降级的助手策略
- **三态模式**：停用/模板/智能灵活切换
- **模板回退**：模板未命中自动回退智能模式
- **成本控制**：模板模式下不调用LLM，降低成本

#### 3. 工程化RAG管道
- **双检索模式**：关键词与向量检索可配置切换
- **自动降级**：向量检索失败自动回退关键词
- **增量同步**：商品变更自动upsert/delete向量

#### 4. 流畅的用户体验
- **SSE流式响应**：打字机效果提升体验
- **语义单元切分**：英文单词完整推送，中文逐字推送
- **商品卡片导流**：消息下挂可点击商品推荐

#### 5. 安全防护机制
- **入站内容检测**：LLM辅助检测敏感内容
- **订单安全隔离**：必须携带userId才能查询订单
- **JWT鉴权**：接口级权限控制

---

## 第22页：演示方案与答辩话术

### 演示路线设计

**开场30秒（核心价值点）**
> "本项目的智能助手不是孤立的聊天框，后端每次对话都会先查询上架商品与用户订单，将真实业务数据写入提示词后再调用通义千问。因此商品推荐和订单状态都有据可查，有效控制了大语言模型的幻觉问题。"

**演示步骤**

| 步骤 | 演示内容 | 话术要点 |
|------|----------|----------|
| 1 | 首页 → 智能助手入口 | 展示系统整体架构 |
| 2 | 流式模式提问："推荐便宜的键盘" | 展示关键词打分、流式打字效果、商品卡片 |
| 3 | 查询订单："我的订单状态" | 展示订单RAG检索、履约说明 |
| 4 | 查询具体订单："ORD202401010001" | 展示订单号正则匹配、结构化输出 |
| 5 | 切换普通模式 | 对比两种响应方式 |
| 6 | 管理端设置 | 展示停用/模板/智能三态切换 |
| 7 | 内容检测（可选） | 展示敏感词拦截 |

### 典型演示问题准备

1. **商品推荐**："推荐一款性价比高的手机"
2. **订单查询**："我的订单状态是什么"
3. **订单详情**："ORD202401010001到哪了"
4. **购物引导**："我想买一个生日礼物，有什么推荐"
5. **价格咨询**："最便宜的键盘多少钱"

### 预期输出示例

**用户**：推荐一款便宜的键盘

**助手**：
> 思路：用户想要便宜的键盘，我需要查看商品检索结果中的低价商品，根据价格排序后推荐性价比高的选项。
>
> 回答：根据您的需求，为您推荐以下高性价比键盘：
> 1. 无线键盘 - ¥99，简约设计静音按键，库存100件
> 2. 蓝牙键盘 - ¥129，支持多设备连接，库存80件
> 
> （商品卡片：可点击跳转详情）

---

## 第23页：不足与改进方向

### 当前不足分析

| 维度 | 现状 | 问题 |
|------|------|------|
| 用户身份 | 助手路径JWT放行+默认userId | 存在安全隐患，生产需强制登录 |
| 物流信息 | 状态文案推导 | 未对接真实快递轨迹API |
| RAG检索 | 商品关键词+订单规则触发 | 检索精度有限 |
| 内容安检 | 每条消息调LLM | 成本高、延迟大 |
| 历史拼接 | 当前消息可能重复出现在history与prompt | 冗余计算 |
| 向量同步 | 依赖外部服务 | 同步成本高 |

### 改进方向

| 改进点 | 方案 | 预期效果 |
|--------|------|----------|
| 用户认证 | 强制JWT，删除默认用户，会话按userId强隔离 | 提升安全性 |
| 物流追踪 | 对接快递100、菜鸟等轨迹API | 提供真实物流信息 |
| 检索优化 | 引入ES/BM25混合检索、重排序模型 | 提升检索精度 |
| 安检优化 | 规则引擎+小模型+抽样审核 | 降低成本和延迟 |
| 历史去重 | 重构消息序列，避免重复 | 减少token消耗 |
| 向量同步 | 异步队列、增量同步、分片 | 降低同步成本 |

---

## 第24页：总结与致谢

### 项目成果

1. **完整的电商系统**：用户端+管理端全功能实现，支持商品浏览、购物车、订单、地址、评价、收藏等核心功能

2. **智能助手核心能力**：
   - RAG检索增强（商品+订单双数据源）
   - LLM生成（通义千问qwen-turbo）
   - SSE流式响应（打字机效果）
   - 内容安全检测

3. **可运营的治理能力**：
   - 多模式切换（停用/模板/智能）
   - 内容安全检测拦截
   - 会话管理与历史

4. **工程化实现**：
   - 分层架构清晰
   - 接口化LLM/RAG设计
   - 可配置切换检索模式

### 技术价值

- **业务价值**：降低客服成本，提升用户转化率
- **技术价值**：将大语言模型与业务数据深度融合
- **架构价值**：具备良好的扩展性和可维护性

### 致谢

感谢指导教师XXX的悉心指导！  
感谢评审老师的宝贵时间！

---

## 附录：配置项说明

### 关键配置（application.yml）

```yaml
llm:
  provider: qwen
  qwen:
    api-key: ${LLM_API_KEY}  # 环境变量
    base-url: https://dashscope.aliyuncs.com/api/text-generation/v1

rag:
  search-mode: keyword        # keyword 或 vector
  vector-enabled: false       # 是否启用向量检索
  vector-auto-sync: false     # 启动时自动同步向量

vector-db:
  type: chroma               # chroma 或 milvus
  chroma:
    host: localhost
    port: 8000

embedding:
  provider: qwen
  qwen:
    model-name: text-embedding-v1
```

### 安全提醒
- 数据库密码、Redis密码、大模型API Key使用环境变量配置
- 不得写入公开论文或PPT
- 使用脱敏占位符展示配置项

---

**文档版本**：完整版  
**生成日期**：2026年5月  
**基于代码版本**：工作区 mall-project(1)
