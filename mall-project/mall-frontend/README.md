# 商城智能助手系统 - 前端

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

## 项目结构

```
mall-frontend/
├── src/
│   ├── api/                    # API接口
│   │   ├── request.ts         # Axios封装
│   │   └── assistant.ts       # 智能助手API
│   ├── components/            # 组件
│   │   └── assistant/         # 智能助手相关组件
│   │       ├── ConversationList.vue  # 会话列表
│   │       ├── ChatWindow.vue        # 对话窗口
│   │       └── MessageInput.vue      # 消息输入框
│   ├── layouts/               # 布局组件
│   │   └── MainLayout.vue     # 主布局
│   ├── router/                # 路由配置
│   │   └── index.ts
│   ├── store/                 # Pinia状态管理
│   │   └── user.ts           # 用户状态
│   ├── views/                 # 页面
│   │   ├── Home.vue          # 首页
│   │   ├── Assistant.vue     # 智能助手页面
│   │   ├── ProductList.vue   # 商品列表
│   │   ├── ProductDetail.vue # 商品详情
│   │   ├── Cart.vue          # 购物车
│   │   └── OrderList.vue     # 订单列表
│   ├── App.vue               # 根组件
│   └── main.ts               # 入口文件
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 构建生产版本

```bash
npm run build
```

## 功能模块

### 已实现

- [x] 项目基础框架搭建
- [x] 路由配置
- [x] 状态管理（Pinia）
- [x] API请求封装
- [x] 智能助手页面
- [x] 对话界面（会话列表、消息窗口、输入框）

### 待实现

- [ ] 用户登录/注册
- [ ] 商品列表/详情
- [ ] 购物车功能
- [ ] 订单管理
- [ ] 流式对话（SSE）
- [ ] 商品推荐展示

## 开发说明

### API代理配置

在 `vite.config.ts` 中配置了代理，将 `/api` 请求代理到后端服务 `http://localhost:8080`

### 智能助手使用

1. 进入"智能助手"页面
2. 左侧显示会话列表，可以新建、选择、删除会话
3. 右侧显示对话窗口，输入消息后点击发送或按 Ctrl+Enter 发送
4. 消息会显示在对话窗口中，区分用户消息和助手回复

### 注意事项

- 当前API请求中临时使用 `userId` header，实际应该从JWT token中获取
- 流式对话功能待实现
- 商品推荐功能待实现
