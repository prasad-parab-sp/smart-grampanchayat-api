-- District shard: track who approved a certificate application (Gramsevak workflow).
-- Run once per shard DB that has certificate_application.

ALTER TABLE certificate_application ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ;
ALTER TABLE certificate_application ADD COLUMN IF NOT EXISTS approved_by_user_id UUID;

COMMENT ON COLUMN certificate_application.approved_at IS 'When status became APPROVED';
COMMENT ON COLUMN certificate_application.approved_by_user_id IS 'users.id of approving staff (Gramsevak)';
