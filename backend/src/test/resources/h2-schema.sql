-- ============================================
-- H2 测试 schema（简化版，pgvector 能力由 mock 替代）
-- ============================================
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_session;
DROP TABLE IF EXISTS adoption_application;
DROP TABLE IF EXISTS follow_up_record;
DROP TABLE IF EXISTS pet_personality_tag;
DROP TABLE IF EXISTS pet_embedding;
DROP TABLE IF EXISTS adopter_embedding;
DROP TABLE IF EXISTS knowledge_base;
DROP TABLE IF EXISTS pet_profile;
DROP TABLE IF EXISTS adopter_profile;
DROP TABLE IF EXISTS shelter;
DROP TABLE IF EXISTS "user";
DROP TABLE IF EXISTS mq_message_log;

CREATE TABLE "user" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

CREATE TABLE adopter_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    housing_type VARCHAR(20) NOT NULL,
    has_children BOOLEAN DEFAULT FALSE,
    has_elderly BOOLEAN DEFAULT FALSE,
    has_other_pets BOOLEAN DEFAULT FALSE,
    pet_experience VARCHAR(20) DEFAULT 'NONE',
    daily_hours_available INT DEFAULT 0,
    preferred_pet_size VARCHAR(500),
    preferred_pet_age VARCHAR(500),
    allergy_info VARCHAR(200),
    activity_level VARCHAR(20) DEFAULT 'MODERATE',
    adoption_motivation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pet_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    age_months INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    size VARCHAR(20) NOT NULL,
    health_status VARCHAR(200),
    personality_tags VARCHAR(500),
    description TEXT,
    photos VARCHAR(1000),
    shelter_id BIGINT,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE adoption_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

CREATE TABLE mq_message_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id VARCHAR(100) NOT NULL UNIQUE,
    business_type VARCHAR(50) NOT NULL,
    payload TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    business_id BIGINT,
    result_snapshot TEXT
);

CREATE TABLE chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);