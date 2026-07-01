-- V1__init.sql
-- PawMatch initial schema (pgvector). embedding = VECTOR(2000); HNSW index.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS "user" (
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

CREATE TABLE IF NOT EXISTS pet_profile (
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

CREATE TABLE IF NOT EXISTS pet_embedding (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(2000) NOT NULL,
    model VARCHAR(100) NOT NULL DEFAULT 'Qwen/Qwen3-Embedding-8B',
    dimension INT NOT NULL DEFAULT 4096,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pet_embedding_ivfflat ON pet_embedding USING ivfflat (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_pet_embedding_metadata ON pet_embedding USING gin (metadata);

CREATE TABLE IF NOT EXISTS adopter_profile (
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

CREATE TABLE IF NOT EXISTS adopter_embedding (
    id BIGSERIAL PRIMARY KEY,
    adopter_id BIGINT NOT NULL UNIQUE,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(2000) NOT NULL,
    model VARCHAR(100) NOT NULL DEFAULT 'Qwen/Qwen3-Embedding-8B',
    dimension INT NOT NULL DEFAULT 4096,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_adopter_embedding_ivfflat ON adopter_embedding USING ivfflat (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_adopter_embedding_metadata ON adopter_embedding USING gin (metadata);

CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    chunk_content TEXT,
    metadata JSONB,
    embedding VECTOR(2000),
    model VARCHAR(100),
    dimension INT DEFAULT 4096,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_embedding_ivfflat ON knowledge_base USING ivfflat (embedding vector_cosine_ops) WHERE embedding IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_knowledge_metadata ON knowledge_base USING gin (metadata);

CREATE TABLE IF NOT EXISTS adoption_application (
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

CREATE TABLE IF NOT EXISTS follow_up_record (
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

CREATE TABLE IF NOT EXISTS pet_personality_tag (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    confidence DECIMAL(3,2)
);

CREATE TABLE IF NOT EXISTS shelter (
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

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mq_message_log (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    business_type VARCHAR(50) NOT NULL,
    payload JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    business_id BIGINT,
    result_snapshot JSONB
);

CREATE INDEX IF NOT EXISTS idx_mq_message_log_status ON mq_message_log (status) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_mq_message_log_message_id ON mq_message_log (message_id);