-- =============================================================================
-- Certificate UI / form metadata only (PostgreSQL, district shard).
-- Use: list certificate types, render dynamic fields & document hints, resolve fee.
-- Does NOT include: printable templates, applications, or file storage rows.
-- Prerequisites: tenants(id UUID).
-- =============================================================================

CREATE TABLE IF NOT EXISTS certificate_type (
    id                             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                      UUID REFERENCES tenants (id),
    code                           VARCHAR(64)  NOT NULL,
    category                       VARCHAR(32)  NOT NULL DEFAULT 'CERTIFICATE',
    name_mr                        VARCHAR(300) NOT NULL,
    name_en                        VARCHAR(300),
    description_mr                 TEXT,
    description_en                 TEXT,
    extra_fields_section_title_mr  VARCHAR(300),
    extra_fields_section_title_en  VARCHAR(300),
    default_fee_amount             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    estimated_days_txt             VARCHAR(80),
    icon                           VARCHAR(32),
    sort_order                     INTEGER NOT NULL DEFAULT 0,
    is_active                      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_certificate_type_category CHECK (
        category IN ('CERTIFICATE', 'REGISTRATION', 'PERMISSIONS', 'OTHERS')
    )
);

COMMENT ON TABLE certificate_type IS 'Catalog; tenant_id NULL = platform type.';
COMMENT ON COLUMN certificate_type.icon IS 'Optional emoji or short glyph for catalog UI.';
COMMENT ON COLUMN certificate_type.category IS 'CERTIFICATE, REGISTRATION, PERMISSIONS, or OTHERS.';
COMMENT ON COLUMN certificate_type.extra_fields_section_title_mr IS 'Heading above dynamic fields from certificate_type_field (e.g. अतिरिक्त माहिती).';
COMMENT ON COLUMN certificate_type.extra_fields_section_title_en IS 'Same heading in English when UI is EN.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_type_platform_code
    ON certificate_type (code)
    WHERE tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_type_tenant_code
    ON certificate_type (tenant_id, code)
    WHERE tenant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_certificate_type_active_sort
    ON certificate_type (is_active, sort_order)
    WHERE is_active;


CREATE TABLE IF NOT EXISTS tenant_certificate_type_config (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID NOT NULL REFERENCES tenants (id),
    certificate_type_id  UUID NOT NULL REFERENCES certificate_type (id),
    fee_amount           NUMERIC(12, 2) NOT NULL,
    is_enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_tenant_fee_non_negative CHECK (fee_amount >= 0)
);

COMMENT ON TABLE tenant_certificate_type_config IS 'Per-tenant fee + visibility for a platform certificate_type.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_cert_config_tenant_type
    ON tenant_certificate_type_config (tenant_id, certificate_type_id);


CREATE TABLE IF NOT EXISTS certificate_type_document_hint (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_type_id    UUID NOT NULL REFERENCES certificate_type (id) ON DELETE CASCADE,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    hint_text_mr           TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cert_doc_hint_type ON certificate_type_document_hint (certificate_type_id);


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

CREATE INDEX IF NOT EXISTS idx_cert_type_field_type_sort ON certificate_type_field (certificate_type_id, sort_order);
