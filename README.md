# PawMatch 流浪动物领养智能匹配平台

> 面向宠物领养场景的智能匹配 + RAG 问答平台。
> AI 匹配：向量召回 + 规则过滤 + 6 维加权打分 + LLM 生成理由。
> RAG 问答：向量检索 + BM25 全文检索混合召回，LLM 意图识别，模板降级。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.4 + Java 17 + Spring AI 1.1.2 + MyBatis-Plus 3.5.5 |
| AI | 硅基流动 `Qwen3-Embedding-8B` (4096 维) + 阿里 DashScope (ChatModel) |
| 数据库 | PostgreSQL 16 + pgvector (HNSW 索引) |
| 缓存/会话 | Redis 7 |
| 异步 | RabbitMQ 3 (matching_queue / notification_queue / chat_queue) |
| 鉴权 | JWT (jjwt 0.12.6) + BCrypt |
| 迁移 | Flyway 9.x (V1__init + V2__hybrid_search) |
| 测试 | JUnit 5 + Mockito + AssertJ + JaCoCo |
| 前端 | React 18 + TypeScript + Ant Design 5 + Vite + Zustand |

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

## License

MIT