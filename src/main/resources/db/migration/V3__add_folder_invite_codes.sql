CREATE TABLE folder_invite_codes (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    code VARCHAR(32) NOT NULL,
    folder_id VARCHAR(36) NOT NULL REFERENCES folders (id) ON DELETE CASCADE,
    created_by_user_id VARCHAR(36) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT folder_invite_codes_code_unique UNIQUE (code)
);

CREATE INDEX idx_folder_invite_codes_folder_id ON folder_invite_codes (folder_id);
