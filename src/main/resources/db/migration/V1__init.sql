-- Final schema (single baseline). Matches domain entities: string UUIDs VARCHAR(36), Telegram ids as bounded VARCHAR/TEXT.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    telegram_user_id VARCHAR(128) NOT NULL,
    username TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_telegram_user_id_key UNIQUE (telegram_user_id)
);

CREATE TABLE folders (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    parent_id VARCHAR(36) REFERENCES folders (id) ON DELETE CASCADE,
    root_folder_id VARCHAR(36) REFERENCES folders (id) ON DELETE SET NULL,
    shareable BOOLEAN NOT NULL DEFAULT FALSE,
    telegram_chat_id VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT folders_name_nonempty CHECK (char_length(trim(name)) > 0),
    CONSTRAINT folders_no_self_parent CHECK (parent_id IS DISTINCT FROM id),
    CONSTRAINT folders_shareable_only_root CHECK (NOT shareable OR parent_id IS NULL)
);

CREATE UNIQUE INDEX folders_unique_name_root
    ON folders (user_id, lower(trim(name)))
    WHERE parent_id IS NULL;

CREATE UNIQUE INDEX folders_unique_name_under_parent
    ON folders (user_id, parent_id, lower(trim(name)))
    WHERE parent_id IS NOT NULL;

CREATE INDEX idx_folders_user_parent ON folders (user_id, parent_id);

CREATE TABLE files (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    message_id TEXT NOT NULL,
    telegram_file_id VARCHAR(2048) NOT NULL,
    file_name VARCHAR(1024) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(256) NOT NULL DEFAULT 'application/octet-stream',
    folder_id VARCHAR(36) REFERENCES folders (id) ON DELETE SET NULL,
    chunk_group_id VARCHAR(36),
    chunk_index INT,
    chunk_total INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT files_chunk_consistency CHECK (
        (chunk_group_id IS NULL AND chunk_index IS NULL AND chunk_total IS NULL)
        OR (chunk_group_id IS NOT NULL AND chunk_index IS NOT NULL AND chunk_total IS NOT NULL
            AND chunk_index >= 0 AND chunk_total > 0 AND chunk_index < chunk_total)
    )
);

CREATE INDEX idx_files_user ON files (user_id);
CREATE INDEX idx_files_folder ON files (folder_id);
CREATE INDEX idx_files_chunk_group ON files (chunk_group_id);

CREATE TABLE folder_members (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    folder_id VARCHAR(36) NOT NULL REFERENCES folders (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT folder_members_folder_user_unique UNIQUE (folder_id, user_id)
);

CREATE INDEX idx_folder_members_user_id ON folder_members (user_id);
CREATE INDEX idx_folder_members_folder_id ON folder_members (folder_id);
