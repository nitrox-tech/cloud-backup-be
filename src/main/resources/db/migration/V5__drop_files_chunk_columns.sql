-- Remove optional multi-part file metadata (single Telegram message per file row).
ALTER TABLE files DROP CONSTRAINT IF EXISTS files_chunk_consistency;

DROP INDEX IF EXISTS idx_files_chunk_group;

ALTER TABLE files DROP COLUMN IF EXISTS chunk_group_id;
ALTER TABLE files DROP COLUMN IF EXISTS chunk_index;
ALTER TABLE files DROP COLUMN IF EXISTS chunk_total;
