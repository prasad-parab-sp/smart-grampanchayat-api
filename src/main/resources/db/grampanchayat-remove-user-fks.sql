-- =============================================================================
-- District shard: remove redundant user FK columns from grampanchayat.
-- Officers: users table by role. Optional gramsevak_name on grampanchayat — see grampanchayat-officers.sql / grampanchayat-drop-sarpanch-name.sql
-- =============================================================================

ALTER TABLE grampanchayat DROP CONSTRAINT IF EXISTS fk_gp_details_sarpanch;
ALTER TABLE grampanchayat DROP CONSTRAINT IF EXISTS fk_gp_details_deputy_sarpanch;
ALTER TABLE grampanchayat DROP CONSTRAINT IF EXISTS fk_gp_details_gramsevak;
ALTER TABLE grampanchayat DROP CONSTRAINT IF EXISTS fk_gp_details_admin_user;

ALTER TABLE grampanchayat DROP COLUMN IF EXISTS sarpanch_id;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS deputy_sarpanch_id;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS gramsevak_id;
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS admin_user_id;
