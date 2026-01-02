# 商城智能助手系统

基于Spring Boot + Vue3的电商平台，集成大模型智能助手功能。

## 项目结构

```
mall-project/
├── mall-backend/      # Spring Boot后端
├── mall-frontend/     # Vue3前端
└── docs/              # 项目文档
    ├── 商城智能助手系统设计文档.md
    └── 数据库脚本.sql
```

## 技术栈

### 后端
- Spring Boot 2.7+
- MyBatis Plus
- MySQL 8.0
- Redis
- 向量数据库（Chroma/Milvus）
- 大模型API集成

### 前端
- Vue 3
- TypeScript
- Element Plus
- Pinia
- Axios

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis
- Maven 3.6+

### 数据库初始化
1. 执行 `docs/数据库脚本.sql` 创建数据库和表

### 后端启动
```bash
cd mall-backend
mvn clean install
mvn spring-boot:run
```

### 前端启动
```bash
cd mall-frontend
npm install
npm run dev
```

## 功能模块

- [x] 用户管理（注册、登录）
- [x] 商品管理（分类、列表、详情）
- [x] 购物车管理
- [x] 订单管理
- [x] 智能助手（RAG + 大模型）

## 开发计划

详见 `docs/商城智能助手系统设计文档.md`

## 许可证

MIT License
