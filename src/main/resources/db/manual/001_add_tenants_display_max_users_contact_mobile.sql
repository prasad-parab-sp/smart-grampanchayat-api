-- Run against your app database (e.g. grampanchayat). Requires PostgreSQL 11+ for IF NOT EXISTS on ADD COLUMN.
-- If you use Hibernate ddl-auto=update, these may already exist; this script is safe to re-run.

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_users INTEGER;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS contact_mobile VARCHAR(15);
