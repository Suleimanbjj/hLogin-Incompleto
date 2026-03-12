CREATE TABLE IF NOT EXISTS ${table_prefix}accounts (
    unique_id TEXT NOT NULL PRIMARY KEY,
    current_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    hash_algorithm TEXT NOT NULL,
    registration_ip TEXT NULL,
    last_login_ip TEXT NULL,
    first_login INTEGER NULL,
    last_login INTEGER NULL,
    register_date INTEGER NOT NULL,
    language TEXT NULL,
    total_logins INTEGER NOT NULL DEFAULT 0,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    blocked_until INTEGER NULL,
    captcha_successes INTEGER NOT NULL DEFAULT 0,
    captcha_failures INTEGER NOT NULL DEFAULT 0,
    webhook_flags TEXT NULL,
    admin_notes TEXT NULL,
    account_mode TEXT NOT NULL DEFAULT 'OFFLINE',
    last_server TEXT NULL,
    last_known_version TEXT NULL,
    remember_hash TEXT NULL,
    remember_until INTEGER NULL
);

CREATE INDEX IF NOT EXISTS idx_${table_prefix}accounts_normalized_name ON ${table_prefix}accounts(normalized_name);

CREATE TABLE IF NOT EXISTS ${table_prefix}audit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_uuid TEXT NULL,
    subject_name TEXT NULL,
    actor_uuid TEXT NULL,
    action TEXT NOT NULL,
    ip_address TEXT NULL,
    details TEXT NULL,
    created_at INTEGER NOT NULL
);
