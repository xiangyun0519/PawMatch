# 流浪动物领养智能匹配平台 - 完整架构设计文档

> 整理时间：2026-03-23
> 版本：v1.4（四五六模块 + 架构图更新 + RabbitMQ + 知识库）

---

## 一、项目概述

**项目名称：** 流浪动物领养智能匹配平台

**项目定位：** 通过 AI 技术连接救助站与领养家庭，实现宠物与领养人的智能匹配，提高领养成功率

**核心价值：**
- 帮助流浪动物找到合适的归宿
- 减少领养退回事件（匹配不当导致）
- 提升救助站工作效率
- 普及领养知识，提供智能问答服务

---

## 二、整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户层 (User Layer)                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────────────┐   │
│  │ 领养申请者 │ │ 救助站/个人 │ │ 平台管理员 │ │      AI 助手        │   │
│  └────┬────┘ └────┬────┘ └────┬────┘ └──────────┬──────────┘   │
└───────┼────────────┼────────────┼──────────────────┼────────────┘
        │            │            │                  │
        ▼            ▼            ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      前端层 (React + TS)                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │     Web端（管理后台）      │     移动端H5/小程序     │   AI 智能问答侧边栏   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │  HTTP / WebSocket
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API 层 (Spring Boot单体)                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  用户Controller  │  宠物Controller  │  匹配Controller  │   │
│  │  申请Controller  │  聊天Controller  │  通知Controller  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                    统一入口（无需Gateway）                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   用户服务     │    │   宠物服务     │    │   匹配服务     │
│ (User Service) │    │ (Pet Service) │    │(Match Service)│
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                     消息队列层 (RabbitMQ)                       │
│    matching_queue │ notification_queue │ chat_queue              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        数据层 (Data Layer)                       │
│  ┌────────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────┐   │
│  │ PostgreSQL │ │   Redis  │ │  pgvector  │ │  RabbitMQ   │   │
│  │ (主数据)   │ │(缓存/会话)│ │ (向量库)   │ │ (消息队列)   │   │
│  │  + pg     │ │          │ │  + GIN    │ │             │   │
│  └────────────┘ └──────────┘ └────────────┘ └──────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                       AI 能力层 (Spring AI)                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐     │
│  │  智能匹配引擎  │ │  智能问答助手  │ │   文本向量化 Embedding │     │
│  │(Matching AI) │ │(RAG ChatBot) │ │   (Embedding Model)  │     │
│  └──────────────┘ └──────────────┘ └──────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、技术栈选型及理由

### 3.1 后端技术栈

| 技术 | 选择 | 理由 |
|------|------|------|
| 核心框架 | Spring Boot 3.2+ | Java 主流，祥云熟悉，上手快 |
| AI 框架 | Spring AI Alibaba | 对接国内通义大模型，兼容 OpenAI 接口 |
| 向量数据库 | PostgreSQL + pgvector | 存储宠物描述和领养人条件的向量，支持相似度检索 |
| 数据库 | PostgreSQL 16+ | 关系数据存储，功能强大，pgvector 原生支持 |
| 缓存 | Redis | 会话缓存、匹配结果缓存 |
| 消息队列 | RabbitMQ | 异步解耦、流量削峰、应用广泛 |
| ORM | MyBatis-Plus | SQL可控、自动填充、分页插件强大 |

### 3.2 前端技术栈

| 技术 | 选择 | 理由 |
|------|------|------|
| 框架 | React 18 + TypeScript | 主流，类型安全 |
| UI 库 | Ant Design 5 | 阿里出品，企业级组件，B端后台首选 |
| 状态管理 | Zustand + RTK Query | Zustand 轻量够用，RTK Query 封装 API 缓存 |
| 路由 | React Router 6 | SPA 标准 |
| HTTP | Axios | 封装拦截器，统一错误处理 |

---

## 四、核心领域模型

```
┌─────────────────┐       ┌─────────────────┐
│      User       │       │  AdopterProfile │
│   (领养申请人)    │──────<│   (领养人画像)    │
├─────────────────┤       ├─────────────────┤
│ id              │       │ userId (FK)     │
│ username        │       │ housingType     │
│ phone           │       │ hasChildren     │
│ email           │       │ hasElderly      │
│ role            │       │ hasOtherPets     │
│ avatar          │       │ dailyHoursLeft   │
│ createdAt       │       │ activityLevel    │
└─────────────────┘       │ petSizePref     │
                          │ petTypePref     │
                          │ allergyInfo     │
                          │ experienceLevel │
                          └────────┬────────┘
                                   │ 1:N 申请记录
                                   ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│  Application    │       │   PetProfile    │       │ KnowledgeBase   │
│   (领养申请)     │>──────│    (宠物档案)    │       │   (知识库)       │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │       │ id              │
│ adopterId (FK)  │       │ name            │       │ category        │
│ petId (FK)     │       │ species         │       │ title           │
│ status          │       │ breed           │       │ content         │
│ matchingScore   │       │ age             │       │ chunkContent    │
│ message         │       │ gender          │       │ metadata        │
│ createdAt       │       │ size            │       │ embedding       │
│ reviewedBy (FK) │       │ healthStatus    │       │ status          │
│ reviewNote      │       │ personalityTags │       └─────────────────┘
│ reviewAt        │       │ photos[]        │               │ RAG检索
└────────┬────────┘       │ shelterId (FK)  │               ▼
         │                │ status          │       ┌─────────────────┐
         │                └────────┬────────┘       │  ChatSession    │
         │                         │                │  (聊天会话)      │
         ▼                         │                ├─────────────────┤
┌─────────────────┐                │                │ id              │
│  FollowUpRecord │                ▼                │ userId (FK)     │
│    (回访记录)    │       ┌─────────────────┐     │ title           │
├─────────────────┤       │ PetEmbedding     │     │ createdAt       │
│ id              │       │ (宠物向量)        │     └────────┬────────┘
│ applicationId   │       ├─────────────────┤     │ 1:N
│ daysAfterAdopt  │       │ id              │              ▼
│ photos[]        │       │ petId (FK)      │       ┌─────────────────┐
│ petStatus       │       │ content         │       │ ChatMessage     │
│ adopterFeedback │       │ metadata (JSONB)│       │  (聊天消息)      │
│ satisfaction    │       │ embedding       │       ├─────────────────┤
│ nextFollowUpAt │       │ model           │       │ id              │
└─────────────────┘       │ dimension       │       │ sessionId (FK)  │
                          │ createdAt       │       │ role            │
┌─────────────────┐       └─────────────────┘       │ content         │
│     Shelter     │                                │ metadata (JSONB│
│    (救助站)     │──────┐                          │ createdAt       │
├─────────────────┤       │                          └─────────────────┘
│ id              │       │
│ name            │       │
│ type (机构/个人) │       ▼
│ address         │ ┌─────────────────┐
│ contact         │ │PetPersonalityTag│
│ license         │ │  (性格标签)      │
│ verified        │ ├─────────────────┤
└─────────────────┘ │ id              │
                    │ petId (FK)      │
                    │ tag             │
                    │ confidence      │
                    └─────────────────┘

┌─────────────────┐       ┌─────────────────┐
│ MqMessageLog    │       │AdopterEmbedding │
│ (MQ消息追踪)     │       │ (领养人向量)     │
├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │
│ messageId (UK) │       │ adopterId (FK)  │
│ businessType    │       │ content         │
│ payload (JSONB) │       │ metadata (JSONB)│
│ status          │       │ embedding       │
│ retryCount      │       │ model           │
│ errorMessage    │       │ dimension       │
│ createdAt       │       │ createdAt       │
│ processedAt     │       └─────────────────┘
└─────────────────┘
```

---

## 五、AI 匹配系统设计（核心亮点）

### 5.1 匹配流程

```
用户提交领养申请
      │
      ▼
┌───────────────────────────────────┐
│      Step 1: 用户画像向量化         │
│   把领养人填写的条件转成向量         │
│  housingType + hasChildren +       │
│  dailyHours + petSizePref → Embedding│
└────────────────┬──────────────────┘
                  │ 写入 adopter_embedding 表
                  ▼
┌───────────────────────────────────┐
│      Step 2: 向量相似度召回 (RabbitMQ 异步) │
│                                        │
│  用户申请 → 发送 MQ 消息到 matching_queue │
│                                        │
│  消费者收到消息后：                       │
│  ┌─────────────────────────────────┐ │
│  │ 1. 用 GIN 索引过滤 metadata     │ │
│  │    (species, size 等条件)        │ │
│  │ 2. 用 HNSW 索引 ANN 召回         │ │
│  │    Top-K 候选宠物 (K=20~50)      │ │
│  └─────────────────────────────────┘ │
└────────────────┬──────────────────┘
                  │
                  ▼
┌───────────────────────────────────┐
│        Step 3: 规则过滤器           │
│        硬性条件过滤：               │
│   ✗ 对方有小孩 ≠ 用户无小孩         │
│   ✗ 大型犬 ≠ 小公寓                │
│   过敏信息不符 → 直接排除           │
└────────────────┬──────────────────┘
                  │
                  ▼
┌───────────────────────────────────┐
│         Step 4: 精排与打分          │
│        多维度权重打分：              │
│  ┌─────────────────────────────┐  │
│  │  性格匹配度      30%         │  │
│  │  体型偏好匹配    25%         │  │
│  │  经验匹配度      20%         │  │
│  │  活动时间匹配    15%         │  │
│  │  健康状况匹配    10%         │  │
│  └─────────────────────────────┘  │
│     综合得分 = 加权求和 → 0~100分   │
└────────────────┬──────────────────┘
                  │
                  ▼
┌───────────────────────────────────┐
│  Step 5: MQ 消息追踪 + 结果更新     │
│                                        │
│  1. 更新 adoption_application 表     │
│     matching_score = 计算得分         │
│     matching_reasons = 匹配理由        │
│     status = 'PENDING' (待审核)       │
│                                        │
│  2. MQ 消息状态置为 SUCCESS          │
│                                        │
│  3. 发送通知消息到 notification_queue │
└───────────────────────────────────┘
                  │
                  ▼
┌───────────────────────────────────┐
│        Step 6: 结果展示 & 解释       │
│        生成匹配理由：                │
│  "这只柯基性格活泼，适合有时间      │
│   陪伴的用户；与您的住房条件        │
│   和生活方式较为匹配"               │
└───────────────────────────────────┘
```

### 5.2 权重配置（可调整）

```yaml
matching:
  weights:
    personality_match: 0.30    # 性格匹配
    size_preference: 0.25     # 体型偏好
    experience_match: 0.20    # 经验匹配
    activity_match: 0.15      # 活动时间匹配
    health_status: 0.10       # 健康状况
```

---

## 六、RAG 智能问答系统

### 6.1 问答流程

```
用户问："我想领养一只不需要太多运动量的猫"
      │
      ▼
┌─────────────────────────────────────┐
│  意图识别：匹配咨询 / 护理知识 / 流程 │
│        → 识别为：匹配咨询            │
└────────────────┬──────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│     知识库检索（knowledge_base）      │
│                                        │
│  1. 用 GIN 索引过滤 metadata           │
│     category = 'ADOPTION_PROCESS'      │
│                                        │
│  2. 用 HNSW 索引向量相似度检索         │
│     embedding <=> query_embedding      │
│     LIMIT 5                             │
└────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  宠物档案检索（pet_profile + embedding）│
│                                        │
│  1. 用 GIN 索引过滤                   │
│     species = 'CAT'                   │
│     personality_tags 包含 '安静'      │
│                                        │
│  2. 用 HNSW 索引向量相似度检索         │
│     Top-K 推荐宠物                     │
└────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│         LLM 生成回答                  │
│                                        │
│  结合以下内容生成回答：                 │
│  1. 知识库检索结果（领养知识）          │
│  2. 宠物档案检索结果（推荐宠物）        │
│  3. 对话历史上下文                    │
│                                        │
│  → 给出具体推荐 + 护理建议             │
└─────────────────────────────────────┘
```

### 6.2 分块策略

```yaml
chunk_size: 512 tokens        # 每个知识块的大小
chunk_overlap: 50 tokens     # 块之间的重叠（保持上下文连贯）
embedding_model: 国产Embedding服务 (4096维)  # TODO: 替换为实际模型
```

---

## 七、数据库表设计（PostgreSQL 16 + pgvector）

### 7.1 建表语句

```sql
-- ============================================
-- 流浪动物领养平台 - PostgreSQL 建表语句
-- 支持 pgvector 向量检索
-- 注意：不使用外键约束，应用层处理关联关系
-- ============================================

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 用户表（领养申请人 / 救助站管理员）
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'ADOPTER',
    avatar_url VARCHAR(500),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 宠物档案表
CREATE TABLE pet_profile (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    age_months INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    size VARCHAR(20) NOT NULL,
    health_status VARCHAR(200),
    personality_tags JSONB,
    description TEXT,
    photos JSONB,
    shelter_id BIGINT,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 宠物向量表（pgvector 存储，支持 RAG）
CREATE TABLE pet_embedding (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(4096) NOT NULL,
    model VARCHAR(100) NOT NULL DEFAULT '国产Embedding服务',
    dimension INT NOT NULL DEFAULT 4096,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pet_embedding_hnsw ON pet_embedding USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_pet_embedding_metadata ON pet_embedding USING gin (metadata);

-- 领养人画像表
CREATE TABLE adopter_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    housing_type VARCHAR(20) NOT NULL,
    has_children BOOLEAN DEFAULT FALSE,
    has_elderly BOOLEAN DEFAULT FALSE,
    has_other_pets BOOLEAN DEFAULT FALSE,
    pet_experience VARCHAR(20) DEFAULT 'NONE',
    daily_hours_available INT DEFAULT 0,
    preferred_pet_size JSONB,
    preferred_pet_age JSONB,
    allergy_info VARCHAR(200),
    activity_level VARCHAR(20) DEFAULT 'MODERATE',
    adoption_motivation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 领养人向量表（pgvector 存储，支持 RAG）
CREATE TABLE adopter_embedding (
    id BIGSERIAL PRIMARY KEY,
    adopter_id BIGINT NOT NULL UNIQUE,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(4096) NOT NULL,
    model VARCHAR(100) NOT NULL DEFAULT '国产Embedding服务',
    dimension INT NOT NULL DEFAULT 4096,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_adopter_embedding_hnsw ON adopter_embedding USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_adopter_embedding_metadata ON adopter_embedding USING gin (metadata);

-- 宠物知识库表（RAG 核心）
CREATE TABLE knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    chunk_content TEXT,
    metadata JSONB,
    embedding VECTOR(4096),
    model VARCHAR(100),
    dimension INT DEFAULT 4096,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_embedding_hnsw ON knowledge_base USING hnsw (embedding vector_cosine_ops) WHERE embedding IS NOT NULL;
CREATE INDEX idx_knowledge_category ON knowledge_base USING gin (metadata);

-- 领养申请表
CREATE TABLE adoption_application (
    id BIGSERIAL PRIMARY KEY,
    adopter_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    matching_score DECIMAL(5,2),
    matching_reasons TEXT,
    applicant_message TEXT,
    shelter_review_note TEXT,
    reviewed_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 回访记录表
CREATE TABLE follow_up_record (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    days_after_adoption INT NOT NULL,
    photos JSONB,
    pet_health_status VARCHAR(200),
    pet_behavior_status TEXT,
    adopter_feedback TEXT,
    adoption_satisfaction SMALLINT,
    issues_found TEXT,
    next_follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 宠物性格标签表
CREATE TABLE pet_personality_tag (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    confidence DECIMAL(3,2)
);

-- 救助站表
CREATE TABLE shelter (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    address VARCHAR(500),
    contact_phone VARCHAR(20),
    license_number VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天会话表
CREATE TABLE chat_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息表
CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MQ 消息追踪表
CREATE TABLE mq_message_log (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    business_type VARCHAR(50) NOT NULL,
    payload JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_mq_message_log_status ON mq_message_log (status) WHERE status = 'PENDING';
CREATE INDEX idx_mq_message_log_message_id ON mq_message_log (message_id);
```

### 7.2 向量相似度搜索示例

```sql
-- 查询与某领养人最匹配的宠物（余弦相似度）
SELECT
    p.id, p.name, p.species, p.breed, p.age_months, p.size,
    1 - (e.embedding <=> ad.embedding) AS similarity
FROM pet_embedding e
JOIN pet_profile p ON e.pet_id = p.id
JOIN adopter_embedding ad ON ad.adopter_id = ?
WHERE p.status = 'AVAILABLE'
ORDER BY e.embedding <=> ad.embedding
LIMIT 20;
```

---

## 八、API 接口设计

### 8.1 接口列表

**认证模块**
- `POST /api/auth/register` - 注册
- `POST /api/auth/login` - 登录
- `POST /api/auth/logout` - 登出
- `POST /api/auth/refresh` - 刷新Token

**宠物模块**
- `GET /api/pets` - 分页查询宠物列表（多条件筛选）
- `GET /api/pets/{id}` - 获取宠物详情
- `POST /api/pets` - 新增宠物档案（救助站）
- `PUT /api/pets/{id}` - 更新宠物信息
- `DELETE /api/pets/{id}` - 删除宠物档案
- `POST /api/pets/{id}/photos` - 上传宠物照片
- `POST /api/pets/{id}/vector` - 更新宠物向量（自动触发）

**领养人模块**
- `GET /api/adopters/profile` - 获取当前用户画像
- `PUT /api/adopters/profile` - 更新领养人画像
- `POST /api/adopters/profile/vector` - 更新用户向量（自动触发）

**智能匹配模块**
- `POST /api/match/recommend` - 获取推荐宠物列表（AI匹配）
- `POST /api/match/batch` - 批量匹配（救助站视角）
- `GET /api/match/history` - 匹配历史
- `GET /api/match/explain/{petId}` - 获取某个宠物的匹配解释

**申请模块**
- `POST /api/applications` - 提交领养申请
- `GET /api/applications` - 我的申请列表
- `GET /api/applications/{id}` - 申请详情
- `PUT /api/applications/{id}/review` - 审核申请（救助站）
- `PUT /api/applications/{id}/cancel` - 取消申请

**回访模块**
- `POST /api/follow-ups` - 创建回访记录
- `GET /api/follow-ups/{applicationId}` - 获取申请的回访列表
- `PUT /api/follow-ups/{id}` - 更新回访记录

**AI 问答模块**
- `POST /api/chat` - 智能问答
- `GET /api/chat/history/{sessionId}` - 获取对话历史
- `DELETE /api/chat/history/{sessionId}` - 删除对话

**统计模块**
- `GET /api/stats/shelter` - 救助站数据看板
- `GET /api/stats/platform` - 平台整体数据（管理员）

**救助站模块**
- `GET /api/shelters` - 救助站列表
- `GET /api/shelters/{id}` - 救助站详情
- `POST /api/shelters/register` - 注册救助站
- `PUT /api/shelters/{id}` - 更新救助站信息

### 8.2 核心接口详细设计

**POST /api/match/recommend - 智能推荐**

Request:
```json
{
    "adopterId": 12345,
    "limit": 10,
    "filters": {
        "species": ["DOG", "CAT"],
        "size": ["SMALL", "MEDIUM"],
        "maxAgeMonths": 36
    }
}
```

Response:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "pets": [
            {
                "petId": 1001,
                "name": "小白",
                "species": "CAT",
                "breed": "中华田园猫",
                "ageMonths": 12,
                "matchingScore": 92.5,
                "matchingReasons": [
                    "您的住房类型适合养猫",
                    "您的每日空闲时间可以满足猫咪需求",
                    "这只猫性格安静，与您的活动水平匹配"
                ],
                "photo": "https://xxx/cat1.jpg"
            }
        ],
        "totalCount": 25,
        "searchTimeMs": 156
    }
}
```

**POST /api/chat - 智能问答**

Request:
```json
{
    "message": "我想领养一只不需要太多运动量的猫",
    "sessionId": "12345"
}
```

Response:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "reply": "根据您'不需要太多运动量'的需求，我推荐您考虑以下猫咪：\n\n1. **英国短毛猫** - 性格安静，不需大量运动\n2. **布偶猫** - 亲人但不需要太多活动空间\n3. **波斯猫** - 慵懒型，适合室内饲养\n\n您对这些品种感兴趣吗？我可以为您匹配合适的待领养猫咪。",
        "intents": ["匹配咨询"],
        "recommendedPets": [
            {"petId": 1002, "name": "团子", "breed": "英短", "matchScore": 88}
        ]
    }
}
```

---

## 九、项目目录结构

### 9.1 后端（Spring Boot）

```
backend/
├── src/main/java/com/petadopt/
│   ├── PetAdoptApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   ├── RedisConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   └── SpringAIConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── PetController.java
│   │   ├── AdopterController.java
│   │   ├── MatchController.java
│   │   ├── ApplicationController.java
│   │   ├── FollowUpController.java
│   │   ├── ChatController.java
│   │   ├── ShelterController.java
│   │   └── StatsController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── PetService.java
│   │   ├── AdopterService.java
│   │   ├── MatchService.java          # ⭐ AI匹配核心
│   │   ├── ApplicationService.java
│   │   ├── FollowUpService.java
│   │   ├── ChatService.java           # ⭐ RAG问答
│   │   ├── EmbeddingService.java      # ⭐ 向量化服务
│   │   ├── ShelterService.java
│   │   └── StatsService.java
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   ├── PetMapper.java
│   │   ├── PetEmbeddingMapper.java
│   │   ├── AdopterMapper.java
│   │   ├── AdopterEmbeddingMapper.java
│   │   ├── ApplicationMapper.java
│   │   ├── FollowUpMapper.java
│   │   └── ShelterMapper.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── PetProfile.java
│   │   │   ├── PetEmbedding.java
│   │   │   ├── AdopterProfile.java
│   │   │   ├── AdopterEmbedding.java
│   │   │   ├── AdoptionApplication.java
│   │   │   ├── FollowUpRecord.java
│   │   │   ├── Shelter.java
│   │   │   ├── PetPersonalityTag.java
│   │   │   ├── ChatSession.java
│   │   │   └── ChatMessage.java
│   │   ├── dto/request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── MatchRecommendRequest.java
│   │   │   ├── ChatRequest.java
│   │   │   └── PetCreateRequest.java
│   │   ├── dto/response/
│   │   │   ├── LoginResponse.java
│   │   │   ├── MatchResponse.java
│   │   │   ├── ChatResponse.java
│   │   │   └── PetResponse.java
│   │   └── enums/
│   │       ├── UserRole.java
│   │       ├── PetSpecies.java
│   │       ├── PetSize.java
│   │       ├── PetGender.java
│   │       ├── HousingType.java
│   │       ├── ActivityLevel.java
│   │       ├── ApplicationStatus.java
│   │       └── ShelterType.java
│   ├── ai/
│   │   ├── MatchingAI.java             # 匹配AI
│   │   ├── ChatAI.java                 # 对话AI（RAG）
│   │   ├── IntentClassifier.java       # 意图识别
│   │   ├── PromptTemplate.java         # Prompt模板
│   │   └── EmbeddingModel.java         # 向量化模型封装
│   └── common/
│       ├── Result.java
│       ├── PageResult.java
│       └── exception/
│           ├── BusinessException.java
│           ├── ErrorCode.java
│           └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── mapper/
│   ├── promts/
│   └── data/
├── src/test/java/
├── pom.xml
├── docker-compose.yml
└── README.md
```

### 9.2 前端（React）

```
frontend/
├── src/
│   ├── api/
│   │   ├── request.ts
│   │   ├── auth.ts
│   │   ├── pet.ts
│   │   ├── match.ts
│   │   ├── chat.ts
│   │   └── stats.ts
│   ├── pages/
│   │   ├── Home.tsx
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   ├── PetList.tsx
│   │   ├── PetDetail.tsx
│   │   ├── MyProfile.tsx
│   │   ├── MyApplications.tsx
│   │   ├── ShelterDashboard.tsx
│   │   ├── AdminDashboard.tsx
│   │   ├── Chat.tsx
│   │   └── MatchResult.tsx
│   ├── components/
│   │   ├── PetCard.tsx
│   │   ├── PetFilter.tsx
│   │   ├── MatchResult.tsx
│   │   ├── ApplicationForm.tsx
│   │   ├── FollowUpForm.tsx
│   │   ├── ChatBubble.tsx
│   │   ├── ChatInput.tsx
│   │   ├── StatsChart.tsx
│   │   ├── ProfileForm.tsx
│   │   └── Loading.tsx
│   ├── stores/
│   │   ├── authStore.ts        # Zustand - 认证状态
│   │   ├── petStore.ts         # Zustand - 宠物列表状态
│   │   ├── chatStore.ts        # Zustand - 聊天状态
│   │   └── api/                # RTK Query - API 缓存
│   │       ├── petApi.ts
│   │       ├── matchApi.ts
│   │       └── chatApi.ts
│   ├── hooks/
│   │   ├── useMatch.ts
│   │   ├── useChat.ts
│   │   ├── useAuth.ts
│   │   └── usePagination.ts
│   ├── types/
│   │   ├── api.d.ts
│   │   ├── pet.d.ts
│   │   ├── user.d.ts
│   │   ├── match.d.ts
│   │   └── chat.d.ts
│   ├── utils/
│   │   ├── format.ts
│   │   └── validate.ts
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── package.json
├── vite.config.ts
├── tsconfig.json
└── README.md
```

---

## 十、开发周期预估

| 阶段 | 时间 | 内容 |
|------|------|------|
| 第一周 | 环境搭建 + 基础CRUD | 项目初始化、数据库设计、用户/宠物/救助站基础功能 |
| 第二周 | 匹配算法 + 向量库 | Pgvector 接入、Embedding 模型、基础匹配逻辑 |
| 第三周 | AI 问答 + 前端 | RAG 聊天功能、前端页面完善、AI 对话界面 |
| 第四周 | 申请 + 回访 + 完善 | 申请流程、回访系统、数据可视化 |
| 第五周 | 部署 + 优化 | Docker 部署、API 优化、bug 修复 |

---

## 十一、技术决策点

### 11.1 已确认决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 向量数据库 | PostgreSQL + pgvector ✅ | PostgreSQL 内置向量扩展，部署简单 |
| AI 模型 | 通义千问 | 国内访问稳定，成本低 |
| 状态管理 | Zustand + RTK Query | 轻量够用，职责分明 |
| 是否启用 ES | 否（第一阶段） | Pgvector 全文检索足够 |

---

> 📝 版本：v1.4
> 🗓️ 更新日期：2026-03-23
> 📦 更新内容：RabbitMQ + 知识库 + GIN索引 + 四五六模块重构