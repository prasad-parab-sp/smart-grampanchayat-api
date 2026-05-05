-- English columns on certificate_type_field (PostgreSQL). Run once on existing DBs.

ALTER TABLE certificate_type_field ADD COLUMN IF NOT EXISTS label_en VARCHAR(500);
ALTER TABLE certificate_type_field ADD COLUMN IF NOT EXISTS placeholder_en VARCHAR(500);
ALTER TABLE certificate_type_field ADD COLUMN IF NOT EXISTS help_text_en TEXT;
