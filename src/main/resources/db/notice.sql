-- District shard: gram panchayat notices (legacy HTML notices[]).
CREATE TABLE IF NOT EXISTS gp_notice (
    id                  UUID PRIMARY KEY,
    tenant_id           UUID NOT NULL,
    notice_type         VARCHAR(32) NOT NULL,
    title               VARCHAR(500) NOT NULL,
    body                TEXT NOT NULL,
    published_on        DATE NOT NULL,
    expires_on          DATE NOT NULL,
    send_to_citizens    BOOLEAN NOT NULL DEFAULT TRUE,
    send_to_members     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT gp_notice_type_check CHECK (
        notice_type IN ('NOTICE', 'MEETING', 'MEMBER', 'URGENT')
    )
);

CREATE INDEX IF NOT EXISTS idx_gp_notice_tenant_published
    ON gp_notice (tenant_id, published_on DESC);

COMMENT ON TABLE gp_notice IS 'Public notice board entries per tenant (legacy notices array).';
COMMENT ON COLUMN gp_notice.notice_type IS 'NOTICE | MEETING | MEMBER | URGENT';
COMMENT ON COLUMN gp_notice.expires_on IS 'Last day the notice is shown on the public board (inclusive).';
