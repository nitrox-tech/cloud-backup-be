TRUNCATE TABLE folder_invite_codes;

ALTER TABLE folder_invite_codes
    ADD COLUMN telegram_join_link VARCHAR(1000),
    ADD COLUMN expires_at TIMESTAMPTZ;
