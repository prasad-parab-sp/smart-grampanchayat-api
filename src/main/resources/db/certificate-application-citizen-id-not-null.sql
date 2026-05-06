-- Optional migration: enforce NOT NULL on citizen_id for existing databases.
-- Run only after every row has citizen_id set (or delete orphan rows).
-- PostgreSQL will reject this if any NULL remains.

ALTER TABLE certificate_application
    ALTER COLUMN citizen_id SET NOT NULL;
