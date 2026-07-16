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

## License

MIT

---

## W1 阶段：领养 O2O 闭环 + 多角色登录（v1.4+）

W1 把"申请"从 3 态（PENDING / APPROVED / REJECTED）升级为 8 态闭环
`MATCHED → APPLIED → HOME_VISIT_SCHEDULED → HOME_VISIT_PASSED → TRIAL → ADOPTED → FOLLOWING_UP → COMPLETED`，
并引入多角色登录 + 电子协议。详见 [docs/W1-adoption-o2o-design.md](docs/W1-adoption-o2o-design.md) 与 [.trae/specs/w1-adoption-o2o-implementation/](.trae/specs/w1-adoption-o2o-implementation/)。

### 7 天实施记录

| Day | 内容 | 验收要点 | 关键产物 |
|-----|------|---------|----------|
| 1 | V4 Flyway 迁移 + 6 个新 Entity / Mapper + UserRole 扩 MERCHANT + AdoptionApplication 7 字段 | Flyway 启动无报错；`mvn compile` 通过 | [V4__adoption_o2o_and_multi_role.sql](backend/src/main/resources/db/migration/V4__adoption_o2o_and_multi_role.sql) |
| 2 | AdoptionStateMachine + Redisson 分布式锁 + AdoptionStateService + 自动级联 + 事件落地 | 并发 10 线程只有 1 成功；非法转移抛 INVALID_STATE_TRANSITION（HTTP 409） | [AdoptionStateMachine.java](backend/src/main/java/com/petadopt/service/adoption/AdoptionStateMachine.java)、[AdoptionStateService.java](backend/src/main/java/com/petadopt/service/adoption/AdoptionStateService.java) |
| 3 | UserRoleService + JwtUtil claim 扩展（mainRole / currentRole / allRoles）+ @RequiresRole + AuthService 两阶段登录 + IHomeVisitService + MockHomeVisitService（5 秒自动 review） | 多角色登录分两步；旧 token 全部失效 | [JwtUtil.java](backend/src/main/java/com/petadopt/util/JwtUtil.java)、[AuthService.java](backend/src/main/java/com/petadopt/service/AuthService.java)、[MockHomeVisitService.java](backend/src/main/java/com/petadopt/service/adoption/impl/MockHomeVisitService.java) |
| 4 | AdoptionO2oApplicationService + HomeVisitService + AdoptionO2oController 12 接口 | 状态机 8 态全链路主流程跑通 | [AdoptionO2oController.java](backend/src/main/java/com/petadopt/controller/AdoptionO2oController.java) |
| 5 | Flying Saucer + Thymeleaf + AgreementService（PDF 生成 / SHA-256 / 双方签字）+ 联动 ADOPTED | PDF 能打开 + SHA-256 一致；双方签字后状态机切到 ADOPTED | [AgreementService.java](backend/src/main/java/com/petadopt/service/adoption/AgreementService.java)、[adoption-agreement.html](backend/src/main/resources/templates/agreement/adoption-agreement.html) |
| 6 | LoginPage 改造为 Antd Steps + RoleSelector + RoleSwitcher + AdoptionTimeline + AdoptionDetail + HomeVisitSubmit | 登录两步跑通；时间轴渲染正确 | [Login.tsx](frontend/src/pages/Login.tsx)、[AdoptionDetail.tsx](frontend/src/pages/AdoptionDetail.tsx) |
| 7 | AgreementSign 独立页 + ShelterApplicationReview 工作台 + Layout nav 角色化 + 收尾 | `mvn -B -ntp test` 136/0；`npm run build` 成功 | [AgreementSign.tsx](frontend/src/pages/AgreementSign.tsx)、[ShelterApplicationReview.tsx](frontend/src/pages/ShelterApplicationReview.tsx) |

### 关键接口清单

#### 认证（多角色 + 切换）
- `POST /api/auth/login` — 单角色直登 / 多角色返回 `stage=ROLE_SELECTION`
- `POST /api/auth/select-role` — 阶段 2：选择当前角色
- `POST /api/auth/switch-role` — 已登录切换角色
- `GET  /api/auth/me` — 当前用户信息（含 roles / currentRole）

#### 领养 O2O
- `GET  /api/adoption-o2o/my-applications` — ADOPTER 我的申请
- `GET  /api/adoption-o2o/shelter-applications` — SHELTER 本救助站申请
- `GET  /api/adoption-o2o/{id}` — 申请详情（带权限校验）
- `POST /api/adoption-o2o/{id}/submit` — ADOPTER 提交申请
- `POST /api/adoption-o2o/{id}/schedule-visit` — SHELTER 排期家访
- `POST /api/adoption-o2o/{id}/submit-visit` — ADOPTER 提交家访材料
- `POST /api/adoption-o2o/{id}/review-visit` — SHELTER 审核家访
- `POST /api/adoption-o2o/{id}/start-trial` — SHELTER 启动试养
- `POST /api/adoption-o2o/{id}/end-trial` — SHELTER 结束试养
- `GET  /api/adoption-o2o/{id}/agreement` — 协议元信息（按需生成）
- `GET  /api/adoption-o2o/{id}/agreement-pdf` — 协议 PDF（`application/pdf`）
- `POST /api/adoption-o2o/{id}/sign-agreement` — 双方电子签字
- `GET  /api/adoption-o2o/{id}/timeline` — 状态时间轴（基于 `adoption_event`）
- `POST /api/adoption-o2o/{id}/cancel` — 取消申请

#### 前端路由
- `/login` — 登录（两步骤：账号密码 → 角色选择）
- `/adoption/:id` — 申请详情（按 currentRole 显隐操作按钮）
- `/adoption/:id/visit` — 家访材料自助提交
- `/adoption/:id/agreement` — 协议签字独立页
- `/shelter/applications` — 救助站领养工作台（仅 SHELTER / ADMIN 可见）

### 测试统计

- 后端单测：**136 tests, 0 failures, 0 errors**（原 71 → Day 5 末 136，新增 65）
- 前端 build：`npm run build` 成功，dist 体积 1222KB / 379KB gzip
- 状态机单测：合法 8 条 + 非法 5 条
- 并发测试：10 线程同 applicationId 仅 1 成功（Redisson 锁）
- 协议测试：PDF 生成 + SHA-256 一致 + 双方签字触发 ADOPTED

### BREAKING 风险

- `JwtUtil` claim 扩展（mainRole / currentRole / allRoles）：所有已签发 token 立即失效，在线用户全部被踢下线
- 部署建议：低峰期 + 提前公告 + 准备回滚

### 依赖新增

- `org.xhtmlrenderer:flying-saucer-pdf:9.1.22` + `flying-saucer-pdf-itext5`（PDF 生成）
- `org.thymeleaf:thymeleaf:3.1.2.RELEASE`（独立于 Spring MVC 模板引擎）

### W1 不在范围

- 真实志愿者家访网络（保留接口，后续接入）
- 试养期定时器（手动触发）
- 真实 OSS / 邮件 / 短信 / 支付
- W2 / W3 业务（依赖 W1 已就绪的 MERCHANT 角色 + 多角色登录）

---

## W2 阶段：商家服务市场 + 多角色商家（v1.5+）

W2 在 W1 多角色登录之上，启用 `MERCHANT` 角色（`UserRole.MERCHANT` 在 W1 已加），新增"领养后服务市场"完整闭环：
商家入驻 → 资质审核 → 上架服务 → 时段管理 → 用户下单 → 自动确认 → 核销码核销 → 评价 → 异步评分聚合。
详见 [docs/W2-service-marketplace-design.md](docs/W2-service-marketplace-design.md) 与 [.trae/specs/w2-service-marketplace-implementation/](.trae/specs/w2-service-marketplace-implementation/)。

### 7+1 天实施记录

| Day | 内容 | 验收 | 关键产物 |
|-----|------|--------|----------|
| 1 | V5 Flyway 迁移 + 5 张新表 + 3 个枚举（ServiceCategory / VerifyStatus / BookingStatus）+ 5 个 Entity + 5 个 Mapper + W3 钩子字段（booking_type / target_type / target_id） | Flyway 启动无报错；`flyway_schema_history` 显示 V5 | [V5__service_marketplace.sql](backend/src/main/resources/db/migration/V5__service_marketplace.sql) |
| 2 | ServiceProviderService + ServiceItemService + MerchantController（11 商家端接口）+ 商家端 3 个前端页（Apply/Items/ItemEdit） | 商家能注册 / 上传资料 / 上架下架服务 | [MerchantController.java](backend/src/main/java/com/petadopt/controller/MerchantController.java) |
| 3 | BookingTimeSlotService（批量生成 14 天时段）+ ServiceController（5 个公开接口）+ 用户端 3 个浏览页（ServiceHome/ProviderList/ItemDetail） | 用户能浏览商家 + 服务详情 + 查看可预约时段 | [ServiceController.java](backend/src/main/java/com/petadopt/controller/ServiceController.java) |
| 4 | VoucherService（16 位核销码 + SHA-256 sig 16 位 + QR payload）+ BookingService 接口 + BookingServiceImpl.create（**乐观锁 CAS + Redisson 锁 + 100 线程并发仅 1 成功**）+ MockAutoConfirmDecorator（5s 后自动确认）+ BookingCreate/MyBookings/BookingDetail 3 个前端页 | 用户下单 → 5s 后自动 CONFIRMED + 看到核销码 | [BookingServiceImpl.java](backend/src/main/java/com/petadopt/service/booking/BookingServiceImpl.java) |
| 5 | BookingServiceImpl.redeemVoucher（sig 校验）+ ReviewService + RatingAggregateService（`@EventListener @Async` 异步刷新评分）+ MerchantRedeem + ReviewSubmit + MerchantReviews | 商家输入核销码 → 完成订单 → 用户评价 → 商家评分 1-2 秒内刷新 | [RatingAggregateService.java](backend/src/main/java/com/petadopt/service/booking/RatingAggregateService.java) |
| 6 | AdminMerchantController（4 接口）+ AdminMerchantReview + MerchantStats + Layout nav 角色化（按 `roles[]` 显隐） | ADMIN 能审核 / 看统计 | [AdminMerchantController.java](backend/src/main/java/com/petadopt/controller/AdminMerchantController.java) |
| 7 | 端到端 demo 跑通 + bug 修复 | 用户"找服务 → 下单 → 核销 → 评价" | — |
| 8 | **全链路体检**（与 W1 同款标准）：并发超卖 / sig 校验 / 状态机 / 评价聚合 / W3 钩子 / **拽出 2 个真 bug**（详见 §十一） + 37 个新单测 + 全量验证 | `mvn test` 184→**221**（+37），`npm run build` 成功 | [VoucherServiceTest.java](backend/src/test/java/com/petadopt/service/booking/VoucherServiceTest.java)、[BookingServiceImplTest.java](backend/src/test/java/com/petadopt/service/booking/BookingServiceImplTest.java) |

---

## W3 阶段：救助机构联动 + 整合 Demo（v1.6+）

W3 在 W1/W2 之上启用地图联动 + 探访预约（复用 W2 booking 表的 `booking_type='SHELTER_VISIT'` 钩子）：
shelter 加经纬度 → Leaflet 救助地图 → 机构主页 → 探访时段 → 用户预约 → 机构确认 → 用户到店核销 → 评价 → shelter.rating_avg 聚合。
详见 [docs/W3-shelter-integration-design.md](docs/W3-shelter-integration-design.md) 与 [.trae/specs/w3-shelter-integration-implementation/](.trae/specs/w3-shelter-integration-implementation/)。

### 5+1 天实施记录

| Day | 内容 | 验收 | 关键产物 |
|-----|------|--------|----------|
| 1 | V6 Flyway 迁移 + shelter 表加 14 字段 + booking.provider_id/item_id/slot_id 放空 + 2 张新表（shelter_visit_slot / shelter_review）+ 3 个新 Entity/Mapper + 8 个演示机构 seed（北上杭蓉）+ 14 天时段 seed + 50 条评价 seed + pet_profile.shelter_id 数据回填 | Flyway V6 启动无报错；`\d shelter` 看到新字段 | [V6__shelter_integration.sql](backend/src/main/resources/db/migration/V6__shelter_integration.sql) |
| 2 | ShelterService 改造（LambdaQueryWrapper → QueryWrapper 字符串列名，吸取 W2 教训）+ 新增 ShelterVisitSlotService / ShelterReviewService + ShelterController 扩 11 接口（geoBounds / cities / pets / reviews / visit-slots / visit-slots/batch / visits / visits/confirm / visits/redeem / reviews/reply）+ 前端装 leaflet@1.9.4 + react-leaflet@4.2.1 + ShelterMap / leafletFix（marker 图标 404 修复）+ ShelterMapPage / ShelterDetail + ShelterPortal 6 个页面（Home/Profile/Slots/Visits/Redeem/Stats） | 地图加载 / 标记显示 / 列表联动；SHELTER 工作台能看到本机构预约 | [ShelterController.java](backend/src/main/java/com/petadopt/controller/ShelterController.java)、[ShelterMap.tsx](frontend/src/components/map/ShelterMap.tsx) |
| 3 | VisitBookingService（独立路径，复用 BookingStatus 状态机 + VoucherService + MockNotificationService）+ VisitBookingController 5 接口（create / my / :id / :id/cancel / :id/voucher）+ 前端 VisitBookingCreate / VisitBookingDetail / MyVisits + shelter.ts / visitBookingApi | 用户预约 → 5s 后自动 CONFIRMED → 核销码 + QR 显示 | [VisitBookingService.java](backend/src/main/java/com/petadopt/service/booking/VisitBookingService.java)、[VisitBookingController.java](backend/src/main/java/com/petadopt/controller/VisitBookingController.java) |
| 4-5 | 性能优化（marker 简化 + view_count 自增 + 评分聚合）+ README 收尾 + 截屏预留 | 整页加载 < 2s | — |
| 6 | **全链路体检**：并发超卖 / sig 校验 / 状态机 / shelterId FK 校验 / **拽出 1 个真 bug**（`cancel` operatorUserId 为 null 时绕过权限）+ 31 个新单测 + 全量验证 | `mvn test` 221→**252**（+31），`npm run build` 成功 | [VisitBookingServiceTest.java](backend/src/test/java/com/petadopt/service/booking/VisitBookingServiceTest.java)、[ShelterVisitSlotServiceTest.java](backend/src/test/java/com/petadopt/service/booking/ShelterVisitSlotServiceTest.java)、[ShelterReviewServiceTest.java](backend/src/test/java/com/petadopt/service/booking/ShelterReviewServiceTest.java) |

### 体检修复 1 个真 bug

**Bug C · `VisitBookingService.cancel` 权限绕过（严重）**
- **现象**：`cancel` 方法原逻辑 `if (operatorUserId != null && !operatorUserId.equals(booking.getUserId()))`，
  当 controller 传 null 时跳过校验 → 任何人（无 token）都能取消任意预约。
- **修复**：改为 `if (operatorUserId == null) throw UNAUTHORIZED`，强制登录才能取消。
- **新增单测**：`cancel_null_operator_throws_unauthorized` 覆盖。
- **注**：W2 BookingServiceImpl 同样模式存在此 bug，本期未触碰 W2 代码，纳入后续整治。

### 关键接口清单（19 新）

**公开 7**：`GET /api/shelters/geo-bounds / cities / {id} / {id}/pets / {id}/reviews / {id}/visit-slots / POST {id}/visit-slots/batch(SHELTER)`
**用户 5**：`POST /api/shelters/{id}/reviews / POST /api/shelters/{id}/visits / GET /api/booking/visit/my / GET /api/booking/visit/{id} / POST /api/booking/visit/{id}/cancel / GET /api/booking/visit/{id}/voucher`
**SHELTER 4**：`GET /api/shelters/{id}/visits / POST /api/shelters/visits/{bookingId}/confirm / POST /api/shelters/visits/redeem / POST /api/shelters/reviews/{reviewId}/reply`

### 关键决策

| ID | 决策 | 采用 |
|----|------|------|
| D1 | 探访预约复用 booking 表 | ✓ V5 钩子字段已就位（`booking_type='SHELTER_VISIT'`） |
| D2 | 探访时段独立表 `shelter_visit_slot` | ✓ 商家按服务项 / 机构按机构整体，业务模型不同 |
| D3 | 地图选 Leaflet + react-leaflet + OSM | ✓ 免 API key / 个人商用友好 |
| D4 | ShelterService Wrapper 类型 | ✓ 一律 QueryWrapper 字符串列名（吸取 W2 教训） |
| D5 | booking.slot_id 跨表 FK | ✓ V6 DROP NOT NULL，应用层校验 slot 归属 |
| D6 | booking.provider_id NOT NULL | ✓ V6 ALTER DROP NOT NULL（兼容 SHELTER_VISIT） |
| D7 | shelter.rating_avg 聚合 | ✓ submit 内同步聚合（事务内一次查询，简单可靠） |

### 前端路由（12 新）

`/shelters/map` `/shelters/:id` `/shelters/:id/visit` `/visits/:id` `/visits` `/shelter-portal` `/shelter-portal/profile` `/shelter-portal/slots` `/shelter-portal/visits` `/shelter-portal/redeem` `/shelter-portal/stats`

### 关键依赖

| 层 | 新增 |
|----|------|
| 后端 | 复用 Flyway / MyBatis-Plus / Spring（无新增） |
| 前端 | `leaflet@^1.9.4` + `react-leaflet@^4.2.1` + `@types/leaflet@^1.9.8` (devDep) |
| 地图瓦片 | `https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png` |

### 测试统计

| 模块 | 用例 | W3 新增 |
|------|------|---------|
| VisitBookingService | 15 | +15 |
| ShelterVisitSlotService | 9 | +9 |
| ShelterReviewService | 7 | +7 |
| W1+W2 既有 | 221 | — |
| **总计** | **252** | **+31** |

### BREAKING 风险

| 风险 | 等级 | 缓解 |
|------|------|------|
| V6 shelter 加 14 列 | 低 | IF NOT EXISTS |
| V6 booking DROP NOT NULL provider_id / item_id / slot_id | 中 | 现有数据无 SHELTER_VISIT，迁移前后均能工作 |
| Leaflet marker 图标 404 | 低 | main.tsx 一次性 import leafletFix 配置 |
| OSM 国内访问慢 | 中 | 备选 `tile.openstreetmap.fr/hot` 瓦片源 |