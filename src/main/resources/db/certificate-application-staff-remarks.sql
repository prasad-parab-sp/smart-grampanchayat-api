-- Run once per district shard that has certificate_application.
-- Gramsevak-only remarks (JSON array); citizens see entries after the application is submitted.

ALTER TABLE certificate_application
    ADD COLUMN IF NOT EXISTS staff_remarks_json JSONB NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN certificate_application.staff_remarks_json IS
    'Array of {text, createdAt, createdByUserId} from Gramsevak; appended via approve or POST .../staff-remarks.';
