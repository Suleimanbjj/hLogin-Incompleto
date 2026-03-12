CREATE TABLE IF NOT EXISTS ${table_prefix}accounts (
    unique_id VARCHAR(36) NOT NULL PRIMARY KEY,
    current_name VARCHAR(16) NOT NULL,
    normalized_name VARCHAR(16) NOT NULL,
    password_hash TEXT NOT NULL,
    hash_algorithm VARCHAR(32) NOT NULL,
    registration_ip VARCHAR(64) NULL,
    last_login_ip VARCHAR(64) NULL,
    first_login BIGINT NULL,
    last_login BIGINT NULL,
    register_date BIGINT NOT NULL,
    language VARCHAR(32) NULL,
    total_logins BIGINT NOT NULL DEFAULT 0,
    failed_attempts INT NOT NULL DEFAULT 0,
    blocked_until BIGINT NULL,
    captcha_successes BIGINT NOT NULL DEFAULT 0,
    captcha_failures BIGINT NOT NULL DEFAULT 0,
    webhook_flags TEXT NULL,
    admin_notes TEXT NULL,
    account_mode VARCHAR(16) NOT NULL DEFAULT 'OFFLINE',
    last_server VARCHAR(64) NULL,
    last_known_version VARCHAR(255) NULL,
    remember_hash VARCHAR(255) NULL,
    remember_until BIGINT NULL,
    INDEX idx_${table_prefix}accounts_normalized_name (normalized_name)
);

CREATE TABLE IF NOT EXISTS ${table_prefix}audit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    subject_uuid VARCHAR(36) NULL,
    subject_name VARCHAR(16) NULL,
    actor_uuid VARCHAR(36) NULL,
    action VARCHAR(64) NOT NULL,
    ip_address VARCHAR(64) NULL,
    details TEXT NULL,
    created_at BIGINT NOT NULL
);
