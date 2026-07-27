-- Migration V1: Initial schema

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    birth_date DATE,
    menarche_age INT,
    avg_cycle_days INT DEFAULT 28,
    avg_period_days INT DEFAULT 5,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menstrual_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    flow VARCHAR(10) COMMENT 'LIGHT,MEDIUM,HEAVY',
    pain_level VARCHAR(10) COMMENT 'NONE,MILD,MODERATE,SEVERE',
    color VARCHAR(20),
    clots BIT(1) DEFAULT 0,
    symptoms JSON,
    mood_tags JSON,
    notes TEXT,
    cycle_day INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_records_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_records_user_date (user_id, start_date DESC),
    INDEX idx_records_user_deleted (user_id, deleted),
    INDEX idx_records_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE symptom_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'SYMPTOM,EMOTION',
    color VARCHAR(7) DEFAULT '#409EFF',
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_tags_user_name UNIQUE (user_id, name, type),
    INDEX idx_tags_user_type (user_id, type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cycle_predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    predicted_start_date DATE NOT NULL,
    predicted_end_date DATE,
    ovulation_start DATE,
    ovulation_end DATE,
    fertile_window_start DATE,
    fertile_window_end DATE,
    safe_period_start DATE,
    safe_period_end DATE,
    confidence VARCHAR(20) DEFAULT 'MEDIUM',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_predictions_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_predictions_user_date (user_id, predicted_start_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medication_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    medication_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50),
    taken_at DATETIME NOT NULL,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_medication_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_medication_user_date (user_id, taken_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
