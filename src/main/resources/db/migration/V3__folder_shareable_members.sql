-- shareable: only meaningful on root folders (parent_id IS NULL); client uses false = personal archive, true = shared archive group
ALTER TABLE folders
    ADD COLUMN shareable BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE folders
    ADD COLUMN root_folder_id UUID REFERENCES folders (id) ON DELETE SET NULL;

-- Roots point to themselves; children inherit root of tree
WITH RECURSIVE tree AS (
    SELECT id, parent_id, id AS root_id
    FROM folders
    WHERE parent_id IS NULL
    UNION ALL
    SELECT f.id, f.parent_id, t.root_id
    FROM folders f
             INNER JOIN tree t ON f.parent_id = t.id
)
UPDATE folders fol
SET root_folder_id = tree.root_id
FROM tree
WHERE fol.id = tree.id;

ALTER TABLE folders
    ADD CONSTRAINT folders_shareable_only_root CHECK ( NOT shareable OR parent_id IS NULL );

CREATE TABLE folder_members (
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    folder_id  UUID        NOT NULL REFERENCES folders (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT folder_members_folder_user_unique UNIQUE (folder_id, user_id)
);

CREATE INDEX idx_folder_members_user_id ON folder_members (user_id);
CREATE INDEX idx_folder_members_folder_id ON folder_members (folder_id);
