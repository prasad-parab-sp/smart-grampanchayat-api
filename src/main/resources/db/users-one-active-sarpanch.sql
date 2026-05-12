-- =============================================================================
-- At most ONE active SARPANCH per tenant (other roles unchanged; many GRAMSEVAK OK).
-- Run AFTER fixing duplicate data: see dedupe step below.
-- =============================================================================

-- Step A (run once if needed): keep oldest user id per tenant for active SARPANCH; deactivate extras.
-- WITH ranked AS (
--   SELECT id,
--          ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY id) AS rn
--   FROM users
--   WHERE role = 'SARPANCH'::user_role AND is_active = true
-- )
-- UPDATE users u SET is_active = false
-- FROM ranked r WHERE u.id = r.id AND r.rn > 1;

DROP INDEX IF EXISTS uq_users_one_active_sarpanch_per_tenant;

CREATE UNIQUE INDEX uq_users_one_active_sarpanch_per_tenant
    ON users (tenant_id)
    WHERE role = 'SARPANCH'::user_role AND is_active = true;
