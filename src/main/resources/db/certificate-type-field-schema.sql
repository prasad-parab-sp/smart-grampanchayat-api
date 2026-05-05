-- =============================================================================
-- certificate_type_field only — PostgreSQL district shard.
-- Run when you already have certificate_type but see:
--   ERROR: relation "certificate_type_field" does not exist
-- Prerequisites: certificate_type(id UUID) exists.
-- Idempotent (IF NOT EXISTS).
-- Alternatively run full scripts: certificate-module-form-only.sql or certificate-module.sql
-- =============================================================================

CREATE TABLE IF NOT EXISTS certificate_type_field (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_type_id    UUID NOT NULL REFERENCES certificate_type (id) ON DELETE CASCADE,
    field_key              VARCHAR(120) NOT NULL,
    label_mr               VARCHAR(500) NOT NULL,
    label_en               VARCHAR(500),
    placeholder_mr         VARCHAR(500),
    placeholder_en         VARCHAR(500),
    help_text_mr           TEXT,
    help_text_en           TEXT,
    data_type              VARCHAR(32) NOT NULL,
    -- TEXT, TEXTAREA, DATE, NUMBER, SELECT, FILE
    required               BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    options_json           JSONB,
    max_files              SMALLINT DEFAULT 1,
    max_bytes              BIGINT DEFAULT 5242880,
    allowed_mime_csv       VARCHAR(300),
    CONSTRAINT ck_certificate_type_field_datatype CHECK (
        data_type IN ('TEXT', 'TEXTAREA', 'DATE', 'NUMBER', 'SELECT', 'FILE')
    ),
    CONSTRAINT uq_cert_type_field_key UNIQUE (certificate_type_id, field_key)
);

COMMENT ON COLUMN certificate_type_field.options_json IS 'SELECT: [{"value":"X","label_mr":"…","label_en":"…"}]';

CREATE INDEX IF NOT EXISTS idx_cert_type_field_type ON certificate_type_field (certificate_type_id);
CREATE INDEX IF NOT EXISTS idx_cert_type_field_type_sort ON certificate_type_field (certificate_type_id, sort_order);
