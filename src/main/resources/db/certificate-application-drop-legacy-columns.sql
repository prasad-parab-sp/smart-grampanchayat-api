-- One-time migration per district shard: drop legacy certificate_application columns.
-- Safe to re-run (IF EXISTS).

ALTER TABLE certificate_application
    DROP COLUMN IF EXISTS fee_amount_snapshot,
    DROP COLUMN IF EXISTS fee_was_default_snapshot,
    DROP COLUMN IF EXISTS template_revision_snapshot,
    DROP COLUMN IF EXISTS updated_at;
