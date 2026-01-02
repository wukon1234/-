# 商城智能助手系统 - 后端

## 技术栈

- Spring Boot 2.7.18
- MyBatis Plus
- MySQL 8.0
- Redis
- 大模型API（支持通义千问、OpenAI等）
- 向量数据库（开发阶段使用简单实现，后续可扩展为Chroma/Milvus）

## 项目结构

```
mall-backend/
├── src/main/java/com/mall/
│   ├── MallApplication.java          # 主启动类
│   ├── common/                        # 通用类
│   │   ├── Result.java               # 统一响应结果
│   │   ├── ResultCode.java           # 响应状态码
│   │   └── BusinessException.java    # 业务异常
│   ├── config/                        # 配置类
│   │   ├── CorsConfig.java           # 跨域配置
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   └── LLMConfig.java            # LLM服务配置
│   ├── controller/                    # 控制器
│   │   └── AssistantController.java  # 智能助手控制器
│   ├── service/                       # 服务层
│   │   └── assistant/                # 智能助手服务
│   │       ├── AssistantService.java # 智能助手服务接口
│   │       ├── LLMService.java       # 大模型服务接口
│   │       ├── RAGService.java       # RAG检索服务接口
│   │       └── impl/                 # 实现类
│   ├── mapper/                        # MyBatis Mapper
│   ├── entity/                        # 实体类
│   ├── dto/                           # 数据传输对象
│   └── utils/                         # 工具类
└── src/main/resources/
    └── application.yml                # 配置文件
```

## 配置说明

### 数据库配置

在 `application.yml` 中配置MySQL连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 大模型配置

#### 通义千问配置

```yaml
llm:
  provider: qwen
  qwen:
    api-key: your-qwen-api-key
    base-url: https://dashscope.aliyuncs.com/api/v1
```

#### OpenAI配置（待实现）

```yaml
llm:
  provider: openai
  openai:
    api-key: your-openai-api-key
    base-url: https://api.openai.com/v1
    model: gpt-3.5-turbo
```

## API接口

### 智能助手接口

#### 发送消息

```
POST /api/assistant/chat
Content-Type: application/json
Header: userId: 1

{
  "sessionId": "会话ID（可选）",
  "message": "用户消息"
}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "sessionId": "会话ID",
    "message": "助手回复",
    "relatedProducts": [...],
    "messageId": 1
  }
}
```

## 运行步骤

1. 确保MySQL和Redis已启动
2. 执行数据库脚本创建表和初始数据
3. 在 `application.yml` 中配置数据库连接信息和大模型API密钥
4. 运行 `MallApplication.main()` 启动应用
5. 访问 `http://localhost:8080/api`

## 待实现功能

- [ ] 完整的用户认证（JWT）
- [ ] 向量数据库集成（Chroma/Milvus）
- [ ] Embedding服务实现
- [ ] 流式对话（SSE）
- [ ] 更多大模型支持（OpenAI、文心一言等）
- [ ] 商品、订单等基础业务模块
- [ ] 文件上传功能

## 开发建议

1. **向量数据库集成**：当前RAG服务使用简单实现，建议集成Chroma（开发）或Milvus（生产）
2. **Embedding服务**：需要实现文本向量化服务，可以使用通义千问或OpenAI的Embedding API
3. **流式输出**：实现SSE（Server-Sent Events）支持流式对话
4. **缓存优化**：使用Redis缓存常见问题和商品信息
