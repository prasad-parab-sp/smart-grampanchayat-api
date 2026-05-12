-- District shard: sarpanch display name comes from users (role SARPANCH, active), not grampanchayat.
ALTER TABLE grampanchayat DROP COLUMN IF EXISTS sarpanch_name;
