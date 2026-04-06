-- Supergroup id (-100…) for shareable root archives; all files in the tree are messages in this chat.
ALTER TABLE folders
    ADD COLUMN telegram_chat_id BIGINT NULL;
