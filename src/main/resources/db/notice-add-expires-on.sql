-- District shard: add notice expiry (run after gp_notice exists).
ALTER TABLE gp_notice ADD COLUMN IF NOT EXISTS expires_on DATE;

UPDATE gp_notice
SET expires_on = published_on + INTERVAL '30 days'
WHERE expires_on IS NULL;

ALTER TABLE gp_notice ALTER COLUMN expires_on SET NOT NULL;
