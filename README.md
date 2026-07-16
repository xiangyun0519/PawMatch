# 🐾 PawMatch · 流浪动物领养智能匹配平台

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-HNSW-blue)](https://github.com/pgvector/pgvector)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-252%20passed-brightgreen)]()

**面向宠物领养场景的智能匹配 + RAG 问答平台，覆盖领养 O2O 闭环、商家服务市场、救助机构地图联动。**

[English](#) · [快速开始](#-快速开始) · [核心特性](#-核心特性) · [架构](#-架构) · [路线图](#w1--领养-o2o--w2--商家服务市场--w3--救助机构联动)

</div>

---

## ✨ 这是什么？

PawMatch 是一个全栈宠物领养平台，用 AI 把"找领养 / 找宠物"这件事做得更聪明、更可追溯：

- 🧠 **AI 智能匹配**：向量召回 + 硬规则过滤 + 6 维加权打分 + LLM 生成匹配理由
- 💬 **RAG 知识问答**：向量检索 + BM25 全文混合召回，LLM 意图识别，模板降级
- 📋 **领养 O2O 闭环**：8 态状态机（申请 → 家访 → 试养 → 签约 → 回访），Redisson 分布式锁，电子协议 PDF
- 🏪 **领养后服务市场**：商家入驻 → 服务上架 → 时段管理 → 下单 → 核销 → 评价
- 🗺️ **救助机构地图**：Leaflet + OSM 地图联动、探访预约、机构评分聚合
- 🔒 **安全 / 可观测**：JWT + RBAC、Flyway 迁移、JaCoCo 覆盖率、252 个单元测试

## 🚀 快速开始

> 5 分钟把项目跑起来。**所有密钥都通过环境变量注入，仓库不含任何真实凭证。**

### 前置环境

- JDK 17+ / Maven 3.9+
- Node.js 20+ / npm
- Docker & Docker Compose

### 1. 启动基础设施

```bash
cd backend
docker-compose up -d
# 自动拉起：PostgreSQL 16 + pgvector / Redis 7 / RabbitMQ 3
```

### 2. 配置本地密钥（必填，从未提交到仓库）

```bash
cd backend
cp .env.example .env
# 用编辑器打开 .env，把下面 4 个值替换成你自己的真实密钥：
#   - OPENAI_API_KEY          硅基流动 API Key
#   - DASHSCOPE_API_KEY       阿里云 DashScope / 百炼 API Key
#   - ALI_OSS_ACCESS_KEY_ID   阿里云 OSS AccessKey ID
#   - ALI_OSS_ACCESS_KEY_SECRET 阿里云 OSS AccessKey Secret
# 其它（数据库 / Redis / JWT）保持默认即可
```

### 3. 启动后端

```bash
cd backend
bash run.sh
# 自动加载 .env，启动 Spring Boot
# → http://localhost:8081
# → Swagger UI: http://localhost:8081/swagger-ui.html
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### 5. 验证

```bash
# 注册 + 登录（拿到 JWT token）
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Pass1234","email":"a@b.com"}'

# 发起匹配（异步）
curl -X POST 'http://localhost:8081/api/match/recommend?topK=5' \
  -H "Authorization: Bearer <token>"
```

更多细节见下方 [目录结构](#目录结构) / [测试](#测试) / [重构 TODO](#重构-todo-总览) / [W1](#w1-阶段领养-o2o-闭环--多角色登录v14) / [W2](#w2-阶段商家服务市场--多角色商家v15) / [W3](#w3-阶段救助机构联动--整合-demov16)。

---

## 🧩 核心特性

### 1. AI 智能匹配（异步流水线）

```
POST /api/match/recommend (topK)
  → 立即返回 taskId（< 200ms）
  → matching_queue 消费者执行：
      ① 向量召回（HNSW, topK*2 候选）
      ② 硬规则过滤（过敏/住房体型/状态/偏好排斥）
      ③ 6 维加权打分（性格30/体型25/经验20/活动15/健康10）
      ④ 匹配理由（Redis 缓存 → LLM → 模板降级）
  → 前端轮询 GET /api/match/history/{taskId}
```

### 2. RAG 智能问答（混合检索）

```
用户问题
  → IntentClassifier（LLM 主 + 关键词 fallback）
  → HybridSearchService：
      Stage 1: 向量召回（HNSW top-20）+ BM25（tsvector top-20）
      Stage 2: RRF 融合去重
      Stage 3: top-5 注入 prompt
  → LLM 生成答案
```

### 3. 领养转化漏斗

`GET /api/stats/matching-funnel` 返回：发起匹配数 → 申请数 → 审核通过数 → 完成领养数 + 各级转化率。

---

## 🏗️ 架构

```
┌─────────────────────────────────────────────────┐
│ Frontend (React + TS + Antd + Leaflet)          │
└─────────────────┬───────────────────────────────┘
                  │ JWT
                  ▼
┌─────────────────────────────────────────────────┐
│ Spring Boot (REST + WebSocket)                  │
│  ├─ Security (JWT + RBAC + 多角色切换)          │
│  ├─ Controllers (9+)                            │
│  ├─ Services (Matching / HybridSearch / ETL)    │
│  └─ MQ Producers / Consumers                    │
└───┬──────────┬────────────┬─────────────┬───────┘
    │          │            │             │
    ▼          ▼            ▼             ▼
┌───────┐ ┌────────┐ ┌────────────┐ ┌──────────┐
│ PG +  │ │ Redis  │ │ RabbitMQ   │ │ Spring AI│
│vector │ │        │ │ (3 queue)  │ │ Qwen3 +  │
└───────┘ └────────┘ └────────────┘ │ DashScope│
                                   └──────────┘
```

---

## 🛠️ 技术栈

| 层 | 选型 |
|----|------|
| **后端** | Spring Boot 3.2.4 / Java 17 / Spring AI 1.1.2 / MyBatis-Plus 3.5.5 |
| **AI** | 硅基流动 BAAI/bge-m3 Embedding + DashScope ChatModel |
| **数据库** | PostgreSQL 16 + pgvector (HNSW 索引) |
| **缓存 / 异步** | Redis 7 / RabbitMQ 3 |
| **鉴权** | JWT (jjwt 0.12.6) + BCrypt |
| **迁移** | Flyway 9.x (V1~V6) |
| **PDF** | Flying Saucer + Thymeleaf |
| **分布式锁** | Redisson |
| **测试** | JUnit 5 + Mockito + AssertJ + JaCoCo（**252 tests, 0 failures**）|
| **前端** | React 18 + TypeScript + Ant Design 5 + Vite + Zustand |
| **地图** | Leaflet 1.9 + react-leaflet 4 + OSM 瓦片 |
| **CI / CD** | GitHub Actions + Harbor + K8s + ArgoCD + Helm |

---

## 🤝 贡献

欢迎 PR！提交前请确保：

1. 所有密钥走环境变量，**禁止**在 `application.yml` / 代码里硬编码
2. 新功能必须附带单元测试（项目要求覆盖率 ≥ 25%）
3. 重大变更请先开 Issue 讨论

---

## 📄 License

[MIT](LICENSE)

---

> 📖 **下面是项目的完整技术文档（W1 / W2 / W3 三阶段演进 + 实施记录）**

---

## 目录结构

```
PawMatch1/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/petadopt/
│   │   ├── common/             # Result / 异常
│   │   ├── config/             # 安全 / CORS / RabbitMQ / Swagger
│   │   ├── controller/         # 9 个 Controller
│   │   ├── dto/                # 请求/响应对象
│   │   ├── entity/             # 12 个 Entity
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── model/enums/        # 10 个枚举
│   │   ├── mq/                 # RabbitMQ 生产/消费
│   │   ├── service/            # 核心业务（含 MatchingService/HybridSearchService）
│   │   └── util/               # JwtUtil
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置
│   │   └── db/migration/       # Flyway 迁移
│   ├── src/test/               # 单元测试 + 集成测试
│   └── docker-compose.yml      # PG + pgvector + Redis + RabbitMQ
└── frontend/                   # React + TS + Antd
    └── src/
        ├── api/                # axios 封装
        ├── pages/              # 12 个页面
        └── stores/             # Zustand
```

## 快速开始

### 1. 启动基础设施

```bash
cd backend
docker-compose up -d
# postgres (含 pgvector) + redis + rabbitmq
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# → http://localhost:8080
# → Swagger UI: http://localhost:8080/swagger-ui.html
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### 4. 验证

```bash
# 注册 + 登录
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Pass1234","email":"a@b.com"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Pass1234"}'
# → 拿到 token

# 发起匹配（异步）
curl -X POST 'http://localhost:8080/api/match/recommend?topK=5' \
  -H "Authorization: Bearer <token>"
# → 返回 {taskId, status:"PENDING", pollUrl}

# 轮询结果
curl 'http://localhost:8080/api/match/history/<taskId>' \
  -H "Authorization: Bearer <token>"
```

## 核心特性

### 1. AI 智能匹配（异步流水线）

```
POST /api/match/recommend (topK)
  → 立即返回 taskId（< 200ms）
  → 后端 mq_message_log 落库（PENDING）
  → matching_queue 消费者执行：
      ① 向量召回（HNSW, topK*2 候选）
      ② 硬规则过滤（过敏/住房体型/状态/偏好排斥）
      ③ 6 维加权打分（性格30/体型25/经验20/活动15/健康10）
      ④ 匹配理由（Redis 缓存 → LLM → 模板降级）
      ⑤ mq_message_log → SUCCESS（含结果快照）
  → 前端轮询 GET /api/match/history/{taskId}
```

### 2. RAG 智能问答（混合检索）

```
用户问题
  → IntentClassifier（LLM 主 + 关键词 fallback）
  → HybridSearchService：
      Stage 1: 向量召回（HNSW top-20）+ BM25（tsvector top-20）
      Stage 2: RRF 融合去重
      Stage 3: top-5 注入 prompt
  → LLM 生成答案
```

### 3. 匹配转化漏斗

`GET /api/stats/matching-funnel` 返回：
- 发起匹配数
- 提交申请数 + 转化率
- 审核通过数 + 转化率
- 完成领养数 + 转化率
- 整体转化率

## 测试

```bash
cd backend

# 单元测试（66 个用例，< 30s）
mvn test

# 覆盖率报告（target/site/jacoco/index.html）
mvn verify
# BUILD SUCCESS 要求：行覆盖 ≥ 25%

# 跑指定测试
mvn test -Dtest=HardRuleFilterTest
```

## 重构 TODO 总览

完整重构需求见 [docs/analysis/重构需求分析.md](docs/analysis/重构需求分析.md)。

| 阶段 | 任务 | 状态 |
|------|------|------|
| 一 | Flyway / 枚举 / Security / CORS / Swagger | ✅ |
| 二 | RabbitMQ 异步匹配 / HardRuleFilter / 6 维权重 / 模板降级 | ✅ |
| 三 | 知识库 ETL / 混合检索 / LLM 意图 / Chat 历史 | ✅ |
| 四 | 单测 (66) / 集成测试 / JaCoCo / 漏斗 / README | ✅ |
| 五 | Cloud Native（K8s + GitOps + 可观测） | ⏳ 可选 |

## 架构图

```
┌─────────────────────────────────────────────────┐
│ Frontend (React + TS + Antd)                    │
└─────────────────┬───────────────────────────────┘
                  │ JWT
                  ▼
┌─────────────────────────────────────────────────┐
│ Spring Boot (REST + WebSocket)                  │
│  ├─ Security (JWT + RBAC)                       │
│  ├─ Controllers (9)                             │
│  ├─ Services (含 Matching / Hybrid / ETL)       │
│  └─ MQ Pro/Consumers                            │
└───┬──────────┬────────────┬─────────────┬───────┘
    │          │            │             │
    ▼          ▼            ▼             ▼
┌───────┐ ┌────────┐ ┌────────────┐ ┌──────────┐
│ PG +  │ │ Redis  │ │ RabbitMQ   │ │ Spring AI│
│vector │ │        │ │ (3 queue)  │ │ Qwen3-Emb│
└───────┘ └────────┘ └────────────┘ │ + Dash   │
                                   └──────────┘
```
