-- users: PK = system UUID; Telegram id = telegram_user_id (unique)
ALTER TABLE users ADD COLUMN internal_id UUID DEFAULT gen_random_uuid() NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_internal_id_key UNIQUE (internal_id);

ALTER TABLE folders ADD COLUMN new_user_id UUID;
UPDATE folders f
SET new_user_id = u.internal_id
FROM users u
WHERE f.user_id = u.id;

ALTER TABLE folders DROP CONSTRAINT IF EXISTS folders_user_id_fkey;
ALTER TABLE folders DROP COLUMN user_id;
ALTER TABLE folders RENAME COLUMN new_user_id TO user_id;
ALTER TABLE folders ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE files ADD COLUMN new_user_id UUID;
UPDATE files f
SET new_user_id = u.internal_id
FROM users u
WHERE f.user_id = u.id;

ALTER TABLE files DROP CONSTRAINT IF EXISTS files_user_id_fkey;
ALTER TABLE files DROP COLUMN user_id;
ALTER TABLE files RENAME COLUMN new_user_id TO user_id;
ALTER TABLE files ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE users DROP CONSTRAINT users_pkey;
ALTER TABLE users RENAME COLUMN id TO telegram_user_id;
ALTER TABLE users RENAME COLUMN internal_id TO id;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_internal_id_key;
ALTER TABLE users ADD PRIMARY KEY (id);
CREATE UNIQUE INDEX users_telegram_user_id_key ON users (telegram_user_id);

ALTER TABLE folders
    ADD CONSTRAINT folders_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE files
    ADD CONSTRAINT files_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
