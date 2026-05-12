-- =============================================================================
-- Temporary role elevation (e.g. deputy acts as Sarpanch) — district shard DB.
-- Active when: acting_from <= now < acting_until (half-open interval).
-- Clear elevation: SET elevated_role = NULL, acting_from = NULL, acting_until = NULL;
-- Run once per district shard database that has table `users`.
-- =============================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS elevated_role user_role;
ALTER TABLE users ADD COLUMN IF NOT EXISTS acting_from timestamptz;
ALTER TABLE users ADD COLUMN IF NOT EXISTS acting_until timestamptz;

COMMENT ON COLUMN users.elevated_role IS 'Temporary role powers (e.g. SARPANCH) while interval is active';
COMMENT ON COLUMN users.acting_from IS 'Elevation starts at this instant (inclusive)';
COMMENT ON COLUMN users.acting_until IS 'Elevation ends before this instant (exclusive)';
