-- =============================================================================
-- Certificate / दाखले module — district shard (PostgreSQL)
-- Run against each district shard database after core tables exist:
--   tenants (UUID id), citizens (UUID id, optional FK for citizen_id).
-- Requires: PostgreSQL 13+ (gen_random_uuid). For PG14-, enable pgcrypto
--           or replace gen_random_uuid() with uuid_generate_v4() + uuid extension.
-- =============================================================================

-- ── Printable templates (platform default: tenant_id NULL, or tenant-specific) ─────────
CREATE TABLE IF NOT EXISTS certificate_template (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID REFERENCES tenants (id),
    -- NULL = platform-wide default for this shard; set = tenant-owned template variant
    code               VARCHAR(64)  NOT NULL,
    title_mr           VARCHAR(500) NOT NULL,
    body               TEXT         NOT NULL,
    footer             TEXT,
    revision           INTEGER      NOT NULL DEFAULT 1,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE certificate_template IS 'Printable certificate text; tenant_id NULL = platform default.';
CREATE INDEX IF NOT EXISTS idx_certificate_template_tenant ON certificate_template (tenant_id);
CREATE INDEX IF NOT EXISTS idx_certificate_template_code ON certificate_template (code);

-- Platform revision set: tenant_id IS NULL
CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_template_platform_code_rev
    ON certificate_template (code, revision)
    WHERE tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_template_tenant_code_rev
    ON certificate_template (tenant_id, code, revision)
    WHERE tenant_id IS NOT NULL;


-- ── Certificate type (platform catalog OR tenant-added custom type) ─────────
CREATE TABLE IF NOT EXISTS certificate_type (
    id                             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                      UUID REFERENCES tenants (id),
    -- NULL = platform type available to everyone (fee via tenant_certificate_type_config)
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
    template_id                    UUID REFERENCES certificate_template (id),
    sort_order                     INTEGER NOT NULL DEFAULT 0,
    is_active                      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_certificate_type_category CHECK (
        category IN ('CERTIFICATE', 'REGISTRATION', 'PERMISSIONS', 'OTHERS')
    )
);

COMMENT ON COLUMN certificate_type.category IS 'CERTIFICATE, REGISTRATION, PERMISSIONS, or OTHERS.';
COMMENT ON COLUMN certificate_type.icon IS 'Optional emoji or short glyph for catalog UI.';
COMMENT ON COLUMN certificate_type.extra_fields_section_title_mr IS 'Heading above certificate_type_field list.';
COMMENT ON COLUMN certificate_type.extra_fields_section_title_en IS 'Same heading in English when UI is EN.';

COMMENT ON TABLE certificate_type IS 'Catalog entry; tenant_id NULL = platform type.';
COMMENT ON COLUMN certificate_type.default_fee_amount IS 'Used when tenant has no tenant_certificate_type_config row.';

CREATE INDEX IF NOT EXISTS idx_certificate_type_active ON certificate_type (is_active) WHERE is_active;
CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_type_platform_code
    ON certificate_type (code)
    WHERE tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_certificate_type_tenant_code
    ON certificate_type (tenant_id, code)
    WHERE tenant_id IS NOT NULL;


-- ── Per-tenant fee & enable flag for platform certificate types ─────────────
CREATE TABLE IF NOT EXISTS tenant_certificate_type_config (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              UUID NOT NULL REFERENCES tenants (id),
    certificate_type_id    UUID NOT NULL REFERENCES certificate_type (id),
    -- should reference certificate_type rows where tenant_id IS NULL (platform); enforce in app
    fee_amount             NUMERIC(12, 2) NOT NULL,
    is_enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_tenant_fee_non_negative CHECK (fee_amount >= 0)
);

COMMENT ON TABLE tenant_certificate_type_config IS 'Overrides default_fee_amount for platform types per tenant.';

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_cert_config_tenant_type
    ON tenant_certificate_type_config (tenant_id, certificate_type_id);


-- ── Optional: bullet hints “आवश्यक कागदपत्रे” on UI ───────────────────────────
CREATE TABLE IF NOT EXISTS certificate_type_document_hint (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_type_id    UUID NOT NULL REFERENCES certificate_type (id) ON DELETE CASCADE,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    hint_text_mr           TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cert_doc_hint_type ON certificate_type_document_hint (certificate_type_id);


-- ── Extra fields schema (everything except fixed applicant block from UI) ─────
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
    -- e.g. image/jpeg,image/png,application/pdf
    CONSTRAINT ck_certificate_type_field_datatype CHECK (
        data_type IN ('TEXT', 'TEXTAREA', 'DATE', 'NUMBER', 'SELECT', 'FILE')
    ),
    CONSTRAINT uq_cert_type_field_key UNIQUE (certificate_type_id, field_key)
);

COMMENT ON COLUMN certificate_type_field.options_json IS 'For SELECT: [{\"value\":\"X\",\"label_mr\":\"…\",\"label_en\":\"…\"}]';
CREATE INDEX IF NOT EXISTS idx_cert_type_field_type ON certificate_type_field (certificate_type_id);


-- ── Applications (fixed columns mapped from UI) ─────────────────────────────
CREATE TABLE IF NOT EXISTS certificate_application (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                 UUID NOT NULL REFERENCES tenants (id),
    certificate_type_id       UUID NOT NULL REFERENCES certificate_type (id),
    application_number        VARCHAR(40) NOT NULL,
    -- human-readable: e.g. GP/2026/0042
    applicant_full_name     VARCHAR(300) NOT NULL,
    applicant_mobile        VARCHAR(15) NOT NULL,
    reason_short            VARCHAR(200),
    -- कशासाठी / purpose line
    reason_details            TEXT,
    address_text              TEXT,
    for_whom_name             VARCHAR(300),
    -- subject of certificate (“कोणाच्या नावाचा दाखला”)

    citizen_id                UUID REFERENCES citizens (id),
    status                    VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    fee_amount_snapshot       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    fee_was_default_snapshot  BOOLEAN NOT NULL DEFAULT TRUE,
    -- FALSE if tenant_certificate_type_config set fee at submit time

    paid_at                   TIMESTAMPTZ,
    payment_reference         VARCHAR(120),

    additional_values_json    JSONB NOT NULL DEFAULT '{}',
    template_revision_snapshot INTEGER,

    CONSTRAINT ck_certificate_application_status CHECK (
        status IN (
                      'SUBMITTED', 'PENDING_PAYMENT', 'PENDING_REVIEW',
                      'APPROVED', 'REJECTED', 'CANCELLED'
            )
        )
);

COMMENT ON TABLE certificate_application IS 'One row per request; structured extras in additional_values_json; files in certificate_application_file.';
COMMENT ON COLUMN certificate_application.additional_values_json IS 'Keyed by certificate_type_field.field_key; excludes FILE keys (stored as files table).';

CREATE UNIQUE INDEX IF NOT EXISTS uq_cert_app_number_tenant
    ON certificate_application (tenant_id, application_number);

CREATE INDEX IF NOT EXISTS idx_cert_app_tenant_status ON certificate_application (tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_cert_app_mobile ON certificate_application (applicant_mobile);
CREATE INDEX IF NOT EXISTS idx_cert_app_type_date ON certificate_application (certificate_type_id, submitted_at DESC);


-- ── Uploaded files referenced by FILE fields ───────────────────────────────
CREATE TABLE IF NOT EXISTS certificate_application_file (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id       UUID NOT NULL REFERENCES certificate_application (id) ON DELETE CASCADE,
    field_key            VARCHAR(120) NOT NULL,
    ordinal              SMALLINT NOT NULL DEFAULT 1,
    original_filename    VARCHAR(500) NOT NULL,
    content_type         VARCHAR(200) NOT NULL,
    byte_size            BIGINT NOT NULL CHECK (byte_size >= 0),
    storage_bucket       VARCHAR(120),
    storage_key          VARCHAR(900) NOT NULL,
    checksum_sha256      VARCHAR(64),
    uploaded_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_cert_app_file_slot UNIQUE (application_id, field_key, ordinal)
);

CREATE INDEX IF NOT EXISTS idx_cert_app_file_application ON certificate_application_file (application_id);


-- ── Keeps tenant-specific fee history optional (omit if not needed Day 1) ────
-- CREATE TABLE IF NOT EXISTS tenant_certificate_fee_history (...);
