-- Adds optional catalog UI icon to certificate_type (run once per existing district shard).
-- Safe for PostgreSQL 11+ (ADD COLUMN IF NOT EXISTS).

ALTER TABLE certificate_type ADD COLUMN IF NOT EXISTS icon VARCHAR(32);

COMMENT ON COLUMN certificate_type.icon IS 'Optional emoji or short glyph for catalog UI; null = use category default.';
