-- District shard: tax catalog, citizen assessments, and payments.
-- Run once per district database after tenants and citizens exist.

CREATE TABLE IF NOT EXISTS tax_type (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    name_en         VARCHAR(200) NOT NULL,
    name_mr         VARCHAR(200) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tax_type_tenant_active
    ON tax_type (tenant_id, is_active);

COMMENT ON TABLE tax_type IS 'Per-tenant tax catalog (property, water, custom names).';


CREATE TABLE IF NOT EXISTS citizen_tax (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    citizen_id          UUID NOT NULL REFERENCES citizens (id),
    tax_type_id         UUID NOT NULL REFERENCES tax_type (id),
    financial_year      VARCHAR(9) NOT NULL,
    assessment_number   VARCHAR(64),
    amount_assessed     NUMERIC(14, 2) NOT NULL,
    amount_outstanding  NUMERIC(14, 2) NOT NULL,
    due_date            DATE NOT NULL,
    status              VARCHAR(16) NOT NULL,
    remarks             TEXT,
    created_by_user_id  UUID REFERENCES users (id),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT citizen_tax_status_check CHECK (
        status IN ('PENDING', 'PARTIAL', 'PAID', 'WAIVED', 'CANCELLED')
    ),
    CONSTRAINT citizen_tax_amounts_nonneg CHECK (
        amount_assessed >= 0 AND amount_outstanding >= 0
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_citizen_tax_citizen_type_fy
    ON citizen_tax (citizen_id, tax_type_id, financial_year)
    WHERE status <> 'CANCELLED';

CREATE INDEX IF NOT EXISTS idx_citizen_tax_tenant_citizen
    ON citizen_tax (tenant_id, citizen_id);

CREATE INDEX IF NOT EXISTS idx_citizen_tax_tenant_status_due
    ON citizen_tax (tenant_id, status, due_date);


CREATE TABLE IF NOT EXISTS tax_payment (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    citizen_tax_id      UUID NOT NULL REFERENCES citizen_tax (id),
    amount              NUMERIC(14, 2) NOT NULL,
    paid_on             DATE NOT NULL,
    payment_mode        VARCHAR(16) NOT NULL,
    receipt_number      VARCHAR(32) NOT NULL,
    reference           VARCHAR(100),
    recorded_by_user_id UUID REFERENCES users (id),
    created_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT tax_payment_amount_positive CHECK (amount > 0),
    CONSTRAINT tax_payment_mode_check CHECK (
        payment_mode IN ('CASH', 'UPI', 'CHEQUE', 'ONLINE', 'OTHER')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tax_payment_tenant_receipt
    ON tax_payment (tenant_id, receipt_number);

CREATE INDEX IF NOT EXISTS idx_tax_payment_citizen_tax
    ON tax_payment (citizen_tax_id, paid_on DESC);
