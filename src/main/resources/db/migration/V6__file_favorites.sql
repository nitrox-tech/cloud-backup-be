-- Per-user favorites on file metadata (collaborators can favorite the same file independently).
CREATE TABLE file_favorites (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_id VARCHAR(36) NOT NULL REFERENCES files (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT file_favorites_user_file UNIQUE (user_id, file_id)
);

CREATE INDEX idx_file_favorites_user_id ON file_favorites (user_id);
CREATE INDEX idx_file_favorites_file_id ON file_favorites (file_id);
