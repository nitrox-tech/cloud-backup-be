-- String IDs: internal UUIDs as VARCHAR(36) with DB default; Telegram / opaque ids as TEXT.

ALTER TABLE folder_members DROP CONSTRAINT IF EXISTS folder_members_folder_id_fkey;
ALTER TABLE folder_members DROP CONSTRAINT IF EXISTS folder_members_user_id_fkey;

ALTER TABLE files DROP CONSTRAINT IF EXISTS files_user_id_fkey;
ALTER TABLE files DROP CONSTRAINT IF EXISTS files_folder_id_fkey;

ALTER TABLE folders DROP CONSTRAINT IF EXISTS folders_root_folder_id_fkey;
ALTER TABLE folders DROP CONSTRAINT IF EXISTS folders_parent_id_fkey;
ALTER TABLE folders DROP CONSTRAINT IF EXISTS folders_user_id_fkey;

ALTER TABLE users ALTER COLUMN telegram_user_id TYPE TEXT USING telegram_user_id::text;
ALTER TABLE users ALTER COLUMN id TYPE VARCHAR(36) USING id::text;
ALTER TABLE users ALTER COLUMN id SET DEFAULT (gen_random_uuid()::text);

ALTER TABLE folders ALTER COLUMN id TYPE VARCHAR(36) USING id::text;
ALTER TABLE folders ALTER COLUMN user_id TYPE VARCHAR(36) USING user_id::text;
ALTER TABLE folders ALTER COLUMN parent_id TYPE VARCHAR(36) USING parent_id::text;
ALTER TABLE folders ALTER COLUMN root_folder_id TYPE VARCHAR(36) USING root_folder_id::text;
ALTER TABLE folders ALTER COLUMN telegram_chat_id TYPE TEXT USING telegram_chat_id::text;
ALTER TABLE folders ALTER COLUMN id SET DEFAULT (gen_random_uuid()::text);

ALTER TABLE files ALTER COLUMN id TYPE VARCHAR(36) USING id::text;
ALTER TABLE files ALTER COLUMN user_id TYPE VARCHAR(36) USING user_id::text;
ALTER TABLE files ALTER COLUMN folder_id TYPE VARCHAR(36) USING folder_id::text;
ALTER TABLE files ALTER COLUMN chunk_group_id TYPE VARCHAR(36) USING chunk_group_id::text;
ALTER TABLE files ALTER COLUMN message_id TYPE TEXT USING message_id::text;
ALTER TABLE files ALTER COLUMN id SET DEFAULT (gen_random_uuid()::text);

ALTER TABLE folder_members ALTER COLUMN id TYPE VARCHAR(36) USING id::text;
ALTER TABLE folder_members ALTER COLUMN folder_id TYPE VARCHAR(36) USING folder_id::text;
ALTER TABLE folder_members ALTER COLUMN user_id TYPE VARCHAR(36) USING user_id::text;
ALTER TABLE folder_members ALTER COLUMN id SET DEFAULT (gen_random_uuid()::text);

ALTER TABLE folders
    ADD CONSTRAINT folders_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE folders
    ADD CONSTRAINT folders_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES folders (id) ON DELETE CASCADE;
ALTER TABLE folders
    ADD CONSTRAINT folders_root_folder_id_fkey FOREIGN KEY (root_folder_id) REFERENCES folders (id) ON DELETE SET NULL;

ALTER TABLE files
    ADD CONSTRAINT files_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE files
    ADD CONSTRAINT files_folder_id_fkey FOREIGN KEY (folder_id) REFERENCES folders (id) ON DELETE SET NULL;

ALTER TABLE folder_members
    ADD CONSTRAINT folder_members_folder_id_fkey FOREIGN KEY (folder_id) REFERENCES folders (id) ON DELETE CASCADE;
ALTER TABLE folder_members
    ADD CONSTRAINT folder_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
