CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE folders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    parent_id UUID REFERENCES folders (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT folders_name_nonempty CHECK (char_length(trim(name)) > 0),
    CONSTRAINT folders_no_self_parent CHECK (parent_id IS DISTINCT FROM id)
);

CREATE UNIQUE INDEX folders_unique_name_root
    ON folders (user_id, lower(trim(name)))
    WHERE parent_id IS NULL;

CREATE UNIQUE INDEX folders_unique_name_under_parent
    ON folders (user_id, parent_id, lower(trim(name)))
    WHERE parent_id IS NOT NULL;

CREATE INDEX idx_folders_user_parent ON folders (user_id, parent_id);

CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    message_id BIGINT NOT NULL,
    telegram_file_id TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type TEXT NOT NULL DEFAULT 'application/octet-stream',
    folder_id UUID REFERENCES folders (id) ON DELETE SET NULL,
    chunk_group_id UUID,
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
