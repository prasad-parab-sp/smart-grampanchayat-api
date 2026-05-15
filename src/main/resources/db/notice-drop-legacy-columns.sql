-- Run on district shard if gp_notice was created from an earlier notice.sql revision.
ALTER TABLE gp_notice DROP COLUMN IF EXISTS color;
ALTER TABLE gp_notice DROP COLUMN IF EXISTS use_seal_stamp;
ALTER TABLE gp_notice DROP COLUMN IF EXISTS signature_indexes;
ALTER TABLE gp_notice DROP COLUMN IF EXISTS deleted_at;

DROP INDEX IF EXISTS idx_gp_notice_tenant_published;
CREATE INDEX IF NOT EXISTS idx_gp_notice_tenant_published
    ON gp_notice (tenant_id, published_on DESC);

-- Migrate transliterated enum labels to English (safe if already English).
UPDATE gp_notice SET notice_type = 'NOTICE' WHERE notice_type = 'SUUCHANA';
UPDATE gp_notice SET notice_type = 'MEETING' WHERE notice_type = 'BAITHAK';
UPDATE gp_notice SET notice_type = 'MEMBER' WHERE notice_type = 'SADASYA';
UPDATE gp_notice SET notice_type = 'URGENT' WHERE notice_type = 'TATADI';

ALTER TABLE gp_notice DROP CONSTRAINT IF EXISTS gp_notice_type_check;
ALTER TABLE gp_notice ADD CONSTRAINT gp_notice_type_check CHECK (
    notice_type IN ('NOTICE', 'MEETING', 'MEMBER', 'URGENT')
);

-- Remove one-off citizen banner fields (use gp_notice list instead).
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS online_notice_title;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS online_notice_body;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS online_notice_expires_at;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS online_notice_active;
