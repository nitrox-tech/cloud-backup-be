-- Bounded length for indexing / planner friendlier than unbounded TEXT.
ALTER TABLE folders
    ALTER COLUMN telegram_chat_id TYPE VARCHAR(200);
