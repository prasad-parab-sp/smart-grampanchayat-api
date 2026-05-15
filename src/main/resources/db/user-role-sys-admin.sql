-- Adds system-administrator label to district shard enum {@code user_role} (PostgreSQL).
-- Run once per district database. Safe to re-run: skips if label already exists.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'user_role'
          AND e.enumlabel = 'SYS_ADMIN'
    ) THEN
        ALTER TYPE user_role ADD VALUE 'SYS_ADMIN';
    END IF;
END
$$;
