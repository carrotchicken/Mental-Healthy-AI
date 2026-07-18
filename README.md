# 🧠 AI 心理健康助手 — AI Mental Health Assistant (AMHA)

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vue.js&logoColor=white" alt="Vue">
  <img src="https://img.shields.io/badge/Vite-8.0-646CFF?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Element_Plus-2.13-409EFF?logo=element&logoColor=white" alt="Element Plus">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/FastAPI-0.115-009688?logo=fastapi&logoColor=white" alt="FastAPI">
  <img src="https://img.shields.io/badge/DeepSeek-API-536DFE?logoColor=white" alt="DeepSeek">
  <img src="https://img.shields.io/badge/license-MIT-blue" alt="License">
</p>

<p align="center">
  基于 <b>Vue 3 + Spring Boot + FastAPI</b> 的全栈心理健康服务平台，<br>
  AI 实时对话 + 情绪分析 + 知识库 RAG 检索 + 后台数据管理。
</p>

---

## 📖 目录

- [✨ 功能亮点](#-功能亮点)
- [🏗 项目架构](#-项目架构)
- [📂 项目结构](#-项目结构)
- [🛠 技术栈](#-技术栈)
- [🚀 快速启动](#-快速启动)
- [📡 API 接口文档](#-api-接口文档)
- [🔐 安全设计](#-安全设计)
- [📝 开发命令参考](#-开发命令参考)
- [🙏 致谢](#-致谢)
- [📝 License](#-license)

---

## ✨ 功能亮点

### 🤖 AI 心理咨询
- 基于 **SSE（Server-Sent Events）** 实时流式推送，AI 回复逐字呈现（打字机效果）
- 支持**多轮上下文**对话，保持对话连贯性
- **多会话管理**：创建 / 切换 / 删除独立咨询会话，互不干扰
- 对话结束后自动生成**情绪分析报告**，追踪心理状态变化

### 📔 情绪日记
- 每日记录情绪状态（喜/怒/哀/惧/爱/恶/欲 七种基础情绪）
- AI 自动分析日记内容，给出**个性化心理建议**
- 管理员可查看所有用户的情绪趋势与日记分析结果

### 📚 心理健康知识库
- 分类浏览心理学科普文章（情绪管理/人际关系/自我成长/压力应对…）
- 支持文章搜索、详情查看
- **RAG 增强检索**（FAISS 向量索引）：AI 对话时自动检索相关知识，回答更专业
- 管理端支持文章的**富文本编辑**（WangEditor）、分类管理、状态控制

### 📊 管理端数据仪表盘
- **综合数据概览**：用户总数、咨询次数、情绪分布等关键指标
- **ECharts 可视化**：情绪趋势图、活跃度曲线、咨询统计
- 咨询记录管理：查看所有用户的会话详情与消息记录
- 情感分析管理：查阅所有用户的情绪日记与 AI 分析结果

### 👤 用户系统
- **JWT 认证**（jjwt），Token 存储在 localStorage
- **BCrypt** 密码加密存储，不存明文
- **双角色体系**：普通用户（前端功能） + 管理员（后台面板）
- **数据完全隔离**：用户 A 的咨询/日记与用户 B 互不可见
- 路由导航守卫：未登录自动跳转，角色权限严格控制

---

## 🏗 项目架构

```
┌─────────────────────────────────────────────────────────┐
│                    用户浏览器（桌面端）                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │      Vue 3 SPA (Element Plus + Pinia + ECharts)   │  │
│  │  登录 / 注册 / 首页 / AI对话 / 情绪日记 / 知识库    │  │
│  │  管理后台 / 仪表盘 / 文章管理 / 咨询记录 / 情感分析  │  │
│  └──────────────┬────────────────────────────────────┘  │
│                 │  HTTP + SSE Stream                     │
└─────────────────┼───────────────────────────────────────┘
                  │
     ┌────────────▼────────────┐
     │   Vite Dev Proxy :5173   │
     │   /api/* → backend:8081 │
     └────────────┬────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                  Spring Boot Server :8081                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐ │
│  │ JWT      │ │ BCrypt   │ │ SSE      │ │ ChatAgent  │ │
│  │ 拦截器   │ │ 密码加密 │ │ 流式响应 │ │ AI 接口层  │ │
│  └──────────┘ └──────────┘ └──────────┘ └─────┬──────┘ │
│                                               │         │
│                          ┌────────────────────▼───────┐ │
│                          │    Python FastAPI :5000     │ │
│                          │  LangChain + DeepSeek LLM  │ │
│                          │  FAISS 向量知识库 (RAG)    │ │
│                          └────────────────────────────┘ │
│                                               │         │
│  ┌────────────────────────────────────────────▼───────┐ │
│  │              MySQL 8.0 + Redis                     │ │
│  │  users / chat_sessions / chat_messages /           │ │
│  │  articles / emotion_diaries / categories ...       │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 项目结构

```
AI-Mental-Health-Assistant/
├── frontend/                          # 🖥️ 前端 Vue 3 SPA
│   ├── src/
│   │   ├── views/                     # 页面组件（11 个路由页）
│   │   │   ├── login.vue              #   登录页
│   │   │   ├── register.vue           #   注册页
│   │   │   ├── user/
│   │   │   │   ├── home.vue           #     用户首页
│   │   │   │   ├── consultation.vue   #     AI 对话咨询
│   │   │   │   ├── emotion-diary.vue  #     情绪日记
│   │   │   │   ├── knowledge.vue      #     知识文章列表
│   │   │   │   └── articleDetail.vue  #     文章详情
│   │   │   └── admin/
│   │   │       ├── dashboard.vue      #     数据仪表盘
│   │   │       ├── knowledge.vue      #     文章管理
│   │   │       ├── consultations.vue  #     咨询记录
│   │   │       └── emotional.vue      #     情感分析
│   │   ├── components/                # 通用组件
│   │   │   ├── ArticleDialog.vue      #   文章详情弹窗
│   │   │   ├── MarkdownRenderer.vue   #   Markdown 渲染器
│   │   │   ├── PageHead.vue           #   页面头部
│   │   │   ├── RichTextEditor.vue     #   富文本编辑器（WangEditor）
│   │   │   └── tableSearch.vue        #   表格搜索栏
│   │   ├── api/                       # Axios API 接口层
│   │   ├── stores/appStore.ts         # Pinia 全局状态
│   │   ├── router/index.ts            # Vue Router + 导航守卫
│   │   ├── layouts/                   # 布局组件（用户端/管理端/认证）
│   │   ├── styles/                    # 全局 SCSS 样式
│   │   ├── types/                     # TypeScript 类型定义
│   │   ├── utils/                     # 工具函数
│   │   ├── App.vue                    # 根组件
│   │   └── main.ts                    # 入口文件
│   ├── index.html
│   ├── vite.config.ts                 # Vite 构建 + API 代理
│   ├── tsconfig.json
│   └── package.json
├── backend/                           # ⚙️ 后端 Spring Boot 服务
│   ├── src/main/java/com/amha/
│   │   ├── controller/                # REST 控制器
│   │   │   ├── UserController.java    #   用户认证（登录/注册/登出）
│   │   │   ├── ChatController.java    #   AI 对话（SSE 流式）
│   │   │   ├── KnowledgeController.java  # 知识文章 CRUD
│   │   │   ├── EmotionDiaryController.java # 情绪日记
│   │   │   ├── DataAnalyticsController.java # 数据分析
│   │   │   └── FileController.java    #   文件上传
│   │   ├── service/                   # 业务服务层（接口 + 实现）
│   │   ├── entity/                    # MyBatis-Plus 实体类
│   │   ├── mapper/                    # MyBatis-Plus Mapper
│   │   ├── dto/                       # 数据传输对象
│   │   ├── config/                    # 配置（JWT / Web / 数据初始化）
│   │   ├── interceptor/               # JWT 认证拦截器
│   │   ├── agent/                     # AI Agent HTTP 调用层
│   │   └── common/                    # 公共类（Result 封装 / 异常处理）
│   ├── src/main/resources/
│   │   ├── application-example.yml    # 配置文件模板
│   │   ├── application.yml            # ⚠️ 真实配置（不提交 Git）
│   │   └── db/init.sql                # 数据库初始化脚本
│   ├── pom.xml                        # Maven 依赖
│   └── Dockerfile                     # (可选) 容器化
├── agent/                             # 🧠 AI Agent 服务 (Python FastAPI)
│   ├── app.py                         # FastAPI 入口 + 路由 + lifespan
│   ├── config.py                      # DeepSeek API 配置
│   ├── services/
│   │   ├── agent_service.py           #   AI Agent 核心对话
│   │   ├── chat_service.py            #   聊天服务
│   │   ├── emotion_service.py         #   情绪分析
│   │   ├── diary_service.py           #   日记分析
│   │   └── rag_service.py             #   RAG 知识库检索 (FAISS)
│   ├── prompts/
│   │   ├── chat_prompt.py             #   对话系统提示词
│   │   ├── emotion_prompt.py          #   情绪分析提示词
│   │   └── diary_prompt.py            #   日记分析提示词
│   ├── tools/                         # Agent 工具
│   ├── data/                          # FAISS 索引持久化
│   ├── .env.example                   # 环境变量模板
│   ├── .env                           # ⚠️ 真实配置（不提交 Git）
│   └── requirements.txt               # Python 依赖
├── .gitignore
└── README.md
```

---

## 🛠 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| **前端框架** | Vue 3 + Composition API | 组件化 UI 开发 |
| **类型系统** | TypeScript | 类型安全 |
| **状态管理** | Pinia | 全局应用状态 |
| **UI 组件库** | Element Plus | 桌面端 UI 组件（Table/Form/Dialog/Menu…） |
| **路由** | Vue Router 4 | SPA 页面导航 + 导航守卫 |
| **HTTP 客户端** | Axios | REST API 请求 |
| **SSE 流式** | @microsoft/fetch-event-source | AI 打字机效果 |
| **图表** | ECharts 6 | 数据仪表盘可视化 |
| **富文本编辑** | WangEditor 5 | 知识文章编辑 |
| **Markdown** | 自定义 MarkdownRenderer | AI 回复渲染 |
| **样式** | SCSS | CSS 预处理 |
| **构建工具** | Vite 8 | 极速开发/HMR/打包 |
| **后端框架** | Spring Boot 3.3.5 + JDK 17 | RESTful API 服务 |
| **ORM** | MyBatis-Plus 3.5.7 | 数据库操作 |
| **数据库** | MySQL 8.0 | 主数据存储 |
| **缓存** | Redis 7 | 会话缓存 / 热点数据 |
| **认证** | JWT (jjwt 0.12.6) + BCrypt | 无状态认证 + 密码哈希 |
| **流式对话** | SSE (SseEmitter) + WebFlux | 服务端推送 |
| **AI 引擎** | LangChain + LangChain-OpenAI | LLM 调用抽象层 |
| **LLM** | DeepSeek (deepseek-chat) | 大语言模型 |
| **RAG** | FAISS + sentence-transformers | 向量检索增强生成 |
| **AI 框架** | FastAPI + Uvicorn | Python 异步 API 服务 |
| **工具库** | Hutool 5.8.29 | Java 通用工具 |
| **测试** | JUnit 5 + MockMvc + H2 | 后端单元/集成测试 |

---

## 🚀 快速启动

### 前置要求

| 工具 | 最低版本 | 说明 |
|------|---------|------|
| Node.js | >= 18（推荐 v24） | 前端构建 |
| pnpm | 最新版（或 npm） | 包管理器 |
| JDK | 17 | Java 运行时 |
| Maven | 3.9+ | Java 构建 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7 | 缓存服务 |
| Python | 3.10+ | AI Agent 运行时 |

### 方式一：完整启动（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/ai-mental-health-assistant.git
cd ai-mental-health-assistant

# 2. 初始化数据库
mysql -u root -p < backend/src/main/resources/db/init.sql

# 3. 配置并启动 AI Agent
cd agent
cp .env.example .env
# 编辑 .env 填入你的 DeepSeek API Key → DEEPSEEK_API_KEY=sk-xxx
pip install -r requirements.txt
python app.py
# Agent → http://localhost:5000

# 4. 配置并启动后端（另开终端）
cd backend
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 编辑 application.yml 填入 MySQL 密码 / Redis 地址 / JWT 密钥
mvn spring-boot:run
# 后端 → http://localhost:8081
# 首次启动自动创建表 + 初始化管理员(admin/admin123) + 测试数据

# 5. 启动前端（另开终端）
cd frontend
pnpm install
pnpm dev
# 前端 → http://localhost:5173
# Vite 自动代理 /api 请求到后端 :8081
```

### 方式二：分别启动

| 步骤 | 命令 | 地址 |
|------|------|------|
| 1. 启动 Agent | `cd agent && python app.py` | `http://localhost:5000` |
| 2. 启动后端 | `cd backend && mvn spring-boot:run` | `http://localhost:8081` |
| 3. 启动前端 | `cd frontend && pnpm dev` | `http://localhost:5173` |

> ⚠️ 三者必须**同时运行**。Agent 负责 AI 推理，后端负责业务逻辑并转发 AI 请求，前端负责 UI。

### 测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 管理后台全部功能 |
| 普通用户 | testuser | test123 | 用户端功能 |

---

## 📡 API 接口文档

### 用户认证 `/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/user/login` | 用户登录 | ❌ |
| `POST` | `/user/add` | 用户注册 | ❌ |
| `POST` | `/user/logout` | 用户登出 | ✅ JWT |

**登录请求体：**
```json
{
  "username": "testuser",
  "password": "test123"
}
```

**统一响应格式：**
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "token": "eyJhbGciOiJI..." }
}
```

| code | 含义 |
|------|------|
| `"200"` | 成功 |
| `"-1"` | 登录过期 / 未登录 |
| `"500"` | 业务异常 |

### AI 心理咨询 `/psychological-chat`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/psychological-chat/session/start` | 创建咨询会话 | ✅ |
| `GET` | `/psychological-chat/sessions` | 获取会话列表 | ✅ |
| `DELETE` | `/psychological-chat/sessions/{id}` | 删除会话 | ✅ |
| `GET` | `/psychological-chat/sessions/{id}/messages` | 获取会话消息 | ✅ |
| `POST` | `/psychological-chat/stream` | **SSE 流式对话** | ✅ |
| `GET` | `/psychological-chat/session/{id}/emotion` | 获取情绪分析 | ✅ |

**创建会话请求体：**
```json
{
  "title": "今天心情不太好"
}
```

**SSE 流式对话请求体：**
```json
{
  "sessionId": 1,
  "message": "我最近总是感到很焦虑，不知道该怎么办"
}
```

### 情绪日记 `/emotion-diary`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `POST` | `/emotion-diary` | 提交情绪日记 | ✅ |
| `GET` | `/emotion-diary/admin/page` | 管理员分页查询 | ✅ 管理员 |
| `DELETE` | `/emotion-diary/admin/{id}` | 管理员删除日记 | ✅ 管理员 |

### 知识库 `/knowledge`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/knowledge/category/tree` | 获取分类树 | ❌ |
| `GET` | `/knowledge/article/page` | 文章分页查询 | ❌ |
| `GET` | `/knowledge/article/{id}` | 文章详情 | ❌ |
| `POST` | `/knowledge/article` | 创建文章 | ✅ 管理员 |
| `PUT` | `/knowledge/article/{id}` | 更新文章 | ✅ 管理员 |
| `PUT` | `/knowledge/article/{id}/status` | 修改发布状态 | ✅ 管理员 |
| `DELETE` | `/knowledge/article/{id}` | 删除文章 | ✅ 管理员 |

### 数据分析 `/data-analytics`（管理员）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| `GET` | `/data-analytics/overview` | 仪表盘综合数据 | ✅ 管理员 |

### AI Agent 内部接口 `/api/agent`（后端 ↔ Agent）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/agent/health` | 健康检查 |
| `POST` | `/api/agent/chat` | AI 对话（**SSE 流式**，NDJSON） |
| `POST` | `/api/agent/emotion/analyze` | 情绪分析 |
| `POST` | `/api/agent/diary/analyze` | 日记分析 |
| `POST` | `/api/agent/rag/rebuild` | 全量重建 FAISS 索引 |
| `POST` | `/api/agent/rag/article/add` | 增量添加文章索引 |
| `DELETE` | `/api/agent/rag/article/{id}` | 删除文章索引 |
| `PUT` | `/api/agent/rag/article/{id}` | 更新文章索引 |
| `GET` | `/api/agent/rag/stats` | 查询索引状态 |
| `POST` | `/api/agent/rag/search` | 语义检索（调试用） |

---

## 🔐 安全设计

| 措施 | 说明 |
|------|------|
| **密码加密** | BCrypt 哈希存储，不可逆 |
| **JWT 认证** | 无状态 Token，配置过期时间（默认 24h） |
| **数据隔离** | 每个 API 调用通过 JWT 拦截器注入用户身份，数据库查询强制带 `user_id` 条件 |
| **角色控制** | 双角色体系（userType: 1=用户, 2=管理员），路由守卫 + 后端拦截双重校验 |
| **SQL 注入防护** | MyBatis-Plus 参数化查询 |
| **CORS 配置** | 后端 WebConfig 限制允许的源 |
| **文件上传限制** | 限制文件大小（10MB）与允许的扩展名（jpg/png/gif/webp/pdf/doc/docx） |
| **敏感配置分离** | `.env` / `application.yml` 加入 `.gitignore`，提供 `.example` 模板 |
| **XSS 防护** | 前端 Markdown 渲染时过滤脚本标签 |

---

## 📝 开发命令参考

```bash
# === 前端 ===
cd frontend
pnpm install              # 安装依赖
pnpm dev                  # 启动 Vite 开发服务器 → :5173
pnpm build                # 生产构建
pnpm preview              # 预览生产构建

# === 后端 ===
cd backend
mvn spring-boot:run       # 启动 Spring Boot → :8081
mvn test                  # 运行测试（128 个用例）
mvn clean package         # 打包 JAR

# === AI Agent ===
cd agent
pip install -r requirements.txt   # 安装 Python 依赖
python app.py                     # 启动 FastAPI → :5000
pytest                           # 运行 Agent 测试
```

---

## 🙏 致谢

- [Vue](https://vuejs.org/) — 渐进式 JavaScript 框架
- [Element Plus](https://element-plus.org/) — 桌面端 Vue 3 组件库
- [Spring Boot](https://spring.io/projects/spring-boot) — Java 企业级应用框架
- [MyBatis-Plus](https://baomidou.com/) — 增强型 MyBatis ORM
- [FastAPI](https://fastapi.tiangolo.com/) — 高性能 Python Web 框架
- [LangChain](https://www.langchain.com/) — LLM 应用开发框架
- [DeepSeek](https://www.deepseek.com/) — 高性价比大语言模型
- [FAISS](https://github.com/facebookresearch/faiss) — 高效向量相似度检索

---

## 📝 License

MIT © 2026

---

<p align="center">
  <b>⭐ 如果这个项目对你有帮助，欢迎 Star！</b>
</p>
