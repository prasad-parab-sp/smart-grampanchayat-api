-- District shard: gramsevak display name for certificates when not derived from users.
-- Sarpanch name: use active users.role = SARPANCH for this tenant (see users-one-active-sarpanch.sql).
ALTER TABLE grampanchayat
    ADD COLUMN IF NOT EXISTS gramsevak_name VARCHAR(300);

COMMENT ON COLUMN grampanchayat.gramsevak_name IS 'Optional display name for certificate footer / public UI.';
