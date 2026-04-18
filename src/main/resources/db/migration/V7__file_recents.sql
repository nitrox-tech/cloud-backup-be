-- Per-user recent file metadata touches (e.g. after POST /files/metadata); capped in application to 15 rows per user.
CREATE TABLE file_recents (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_id VARCHAR(36) NOT NULL REFERENCES files (id) ON DELETE CASCADE,
    last_action_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT file_recents_user_file UNIQUE (user_id, file_id)
);

CREATE INDEX idx_file_recents_user_last ON file_recents (user_id, last_action_at DESC);
