# AI Mental Health Assistant (AMHA)

AI 心理健康助手 —— 基于 Vue 3 + Spring Boot + FastAPI 的全栈心理健康服务平台，提供 AI 心理咨询、情绪日记、心理健康知识库等功能，并配备后台数据管理面板。

## 功能概览

### 用户端
- **AI 心理咨询** —— 与 AI 心理助手实时对话，获得情绪支持与建议（SSE 流式响应，集成 DeepSeek LLM）
- **情绪日记** —— 记录每日情绪状态，AI 自动分析情绪并提供个性化建议
- **心理健康知识库** —— 浏览、搜索心理学普及文章
- **用户认证** —— 注册、登录与个人信息管理

### 管理端
- **数据仪表盘** —— 用户活跃度、情绪趋势、咨询统计等可视化分析
- **知识文章管理** —— 文章的增删改查、分类管理、状态控制
- **咨询记录管理** —— 查看所有用户咨询会话与消息详情
- **情感分析管理** —— 查看用户情绪日记与 AI 分析结果

## 项目架构

```
AI Mental Health Assistant/
├── frontend/                # 前端源码 (Vue 3 + TypeScript)
│   ├── src/
│   │   ├── api/             # API 接口层 (Axios)
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── config/          # 配置文件
│   │   ├── layouts/         # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── styles/          # 全局样式 (SCSS)
│   │   ├── types/           # TypeScript 类型定义
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面视图
│   └── vite.config.ts       # Vite 构建配置
├── backend/                 # 后端源码 (Spring Boot 3 + JDK 17)
│   ├── pom.xml              # Maven 配置
│   └── src/main/java/com/amha/
│       ├── agent/           # AI Agent 接口层 (调用 Python Agent)
│       ├── common/          # 公共类 (Result、异常处理)
│       ├── config/          # 配置 (JWT、Web、数据初始化)
│       ├── controller/      # REST 控制器
│       ├── dto/             # 数据传输对象
│       ├── entity/          # 数据库实体
│       ├── interceptor/     # JWT 拦截器
│       ├── mapper/          # MyBatis-Plus Mapper
│       └── service/         # 业务服务层
├── agent/                   # AI Agent 服务 (Python FastAPI)
│   ├── app.py               # FastAPI 入口
│   ├── config.py            # 配置 (DeepSeek API)
│   ├── prompts/             # LLM 提示词模板
│   ├── services/            # 业务服务 (Chat/Emotion/Diary)
│   └── requirements.txt     # Python 依赖
└── README.md
```

## 技术栈

### 前端
| 类别       | 技术                   |
| ---------- | ---------------------- |
| 框架       | Vue 3 + TypeScript     |
| 构建工具   | Vite 8                 |
| UI 组件库  | Element Plus           |
| 状态管理   | Pinia                  |
| 路由       | Vue Router 4           |
| 图表       | ECharts 6              |
| HTTP 请求  | Axios                  |
| 富文本编辑 | WangEditor 5           |
| 样式       | SCSS                   |

### 后端
| 类别     | 技术                            |
| -------- | ------------------------------- |
| 框架     | Spring Boot 3.3.5 + JDK 17      |
| ORM      | MyBatis-Plus 3.5.7              |
| 数据库   | MySQL 8.0                       |
| 缓存     | Redis                           |
| 认证     | JWT (jjwt) + BCrypt             |
| 流式对话 | SSE (SseEmitter) + WebFlux      |
| 测试     | JUnit 5 + MockMvc + H2          |

### AI Agent
| 类别     | 技术                              |
| -------- | --------------------------------- |
| 框架     | FastAPI + Uvicorn                 |
| LLM      | DeepSeek (via LangChain-OpenAI)   |
| 流式输出 | JSON Lines (NDJSON)               |
| 功能     | 对话生成、情绪分析、日记分析      |

## 环境要求

- Node.js >= 18（推荐 v24）
- pnpm（推荐）或 npm
- JDK 17
- Maven 3.9+
- MySQL 8.0
- Redis 7
- Python 3.10+（运行 AI Agent）

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

### 2. 配置 AI Agent

```bash
cd agent
cp .env.example .env
# 编辑 .env 填入你的 DeepSeek API Key
pip install -r requirements.txt
python app.py
```

Agent 运行在 `http://localhost:5000`。

### 3. 配置并启动后端

```bash
cd backend
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 编辑 application.yml 填入你的 MySQL 密码和 JWT 密钥
mvn spring-boot:run
```

后端运行在 `http://localhost:8081`。首次启动会自动初始化管理员账号（admin/admin123）和测试数据。

### 4. 启动前端

```bash
pnpm install
pnpm dev
```

前端运行在 `http://localhost:5173`，Vite 代理将 `/api` 请求转发至后端。

### 5. 登录

| 角色     | 用户名    | 密码      |
| -------- | --------- | --------- |
| 管理员   | admin     | admin123  |
| 普通用户 | testuser  | test123   |

## 后端 API 接口

### 用户模块
| 方法   | 路径          | 说明   |
| ------ | ------------- | ------ |
| POST   | /user/login   | 登录   |
| POST   | /user/add     | 注册   |
| POST   | /user/logout  | 登出   |

### 知识库模块
| 方法   | 路径                           | 说明         |
| ------ | ------------------------------ | ------------ |
| GET    | /knowledge/category/tree       | 分类树       |
| GET    | /knowledge/article/page        | 文章分页查询 |
| POST   | /knowledge/article             | 创建文章     |
| GET    | /knowledge/article/{id}        | 文章详情     |
| PUT    | /knowledge/article/{id}        | 更新文章     |
| PUT    | /knowledge/article/{id}/status | 修改状态     |
| DELETE | /knowledge/article/{id}        | 删除文章     |

### 文件模块
| 方法 | 路径          | 说明     |
| ---- | ------------- | -------- |
| POST | /file/upload  | 文件上传 |

### 情绪日记模块
| 方法   | 路径                      | 说明             |
| ------ | ------------------------- | ---------------- |
| POST   | /emotion-diary            | 提交情绪日记     |
| GET    | /emotion-diary/admin/page | 管理员分页查询   |
| DELETE | /emotion-diary/admin/{id} | 管理员删除日记   |

### 心理咨询模块
| 方法   | 路径                                      | 说明              |
| ------ | ----------------------------------------- | ----------------- |
| POST   | /psychological-chat/session/start         | 创建咨询会话      |
| GET    | /psychological-chat/sessions              | 会话列表          |
| DELETE | /psychological-chat/sessions/{id}         | 删除会话          |
| GET    | /psychological-chat/sessions/{id}/messages| 获取会话消息      |
| POST   | /psychological-chat/stream                | SSE 流式对话      |
| GET    | /psychological-chat/session/{id}/emotion  | 获取情绪分析      |

### 数据分析模块
| 方法 | 路径                       | 说明             |
| ---- | -------------------------- | ---------------- |
| GET  | /data-analytics/overview   | 仪表盘综合数据   |

## API 响应格式

```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {}
}
```

| code  | 含义           |
| ----- | -------------- |
| "200" | 成功           |
| "-1"  | 登录过期/未登录 |
| "500" | 业务异常       |

## 角色说明

| 角色     | userType | 权限                         |
| -------- | -------- | ---------------------------- |
| 普通用户 | 1        | 访问用户端功能               |
| 管理员   | 2        | 访问管理端面板，管理内容与数据 |

## AI Agent 架构

Java 后端通过 `ChatAgent` 接口（`backend/src/main/java/com/amha/agent/`）调用 Python Agent 服务。`RealChatAgent` 通过 WebFlux HTTP 调用 FastAPI 端点：

- `/api/agent/chat` — 流式对话（JSON Lines 格式，Java 端解析后转为 SSE 推送给前端）
- `/api/agent/emotion/analyze` — 情绪分析
- `/api/agent/diary/analyze` — 日记 AI 分析

Python Agent 使用 LangChain 调用 DeepSeek LLM，支持流式输出和结构化分析结果。

## 测试

```bash
cd backend
mvn test
```

共 128 个测试用例，覆盖用户认证、知识库 CRUD、情绪日记、SSE 流式对话、数据分析等全部模块。
