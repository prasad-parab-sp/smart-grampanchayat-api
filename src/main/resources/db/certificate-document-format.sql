-- =============================================================================
-- Admin printable certificate formats (district shard). Run on each shard DB
-- after tenants + certificate_type exist.
-- =============================================================================

CREATE TABLE IF NOT EXISTS certificate_document_format (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL REFERENCES tenants (id),
    certificate_type_id     UUID REFERENCES certificate_type (id),
    display_name            VARCHAR(300) NOT NULL,
    format_kind             VARCHAR(32)  NOT NULL,
    document_title          VARCHAR(500),
    body_html               TEXT         NOT NULL,
    footer_note             TEXT,
    internal_note             TEXT,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_certificate_document_format_kind CHECK (
        format_kind IN ('DAKHALA', 'BILL', 'RECEIPT', 'NOTICE', 'OTHER')
    )
);

COMMENT ON TABLE certificate_document_format IS 'Tenant-owned HTML certificate layouts for preview/issue.';
CREATE INDEX IF NOT EXISTS idx_cert_doc_fmt_tenant ON certificate_document_format (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cert_doc_fmt_cert_type ON certificate_document_format (certificate_type_id);


-- ── Optional dummy rows (safe to re-run: fixed ids + ON CONFLICT DO NOTHING) ───
-- Uses first tenant by tenant_code and first / second active certificate_type when present.
-- If tenants is empty, inserts nothing. If only one certificate_type exists, row 2 has NULL certificate_type_id.

INSERT INTO certificate_document_format (
    id,
    tenant_id,
    certificate_type_id,
    display_name,
    format_kind,
    document_title,
    body_html,
    footer_note,
    internal_note,
    is_active,
    created_at,
    updated_at
)
SELECT
    'f1000000-0000-4000-8000-000000000001'::uuid,
    t.id,
    (SELECT id FROM certificate_type WHERE is_active = true ORDER BY sort_order, name_mr LIMIT 1),
    'डमी — रहिवास दाखला प्रारूप',
    'DAKHALA',
    'रहिवास दाखला',
    '<p>{$header}</p>{$title}<p>&nbsp;</p>'
        || '<p>प्रमाणित केले जाते की [नाव] हे या ग्रामपंचायतीचे कायमचे रहिवासी आहेत.</p>'
        || '<p>&nbsp;</p>'
        || '<p>हा दाखला [कशासाठी] साठी देण्यात येत आहे.</p>'
        || '<p>&nbsp;</p>'
        || '<p>{$footer}</p>',
    '',
    'Seed dummy 1 — linked to first catalog certificate type when available.',
    true,
    now(),
    now()
FROM (SELECT id FROM tenants ORDER BY tenant_code LIMIT 1) AS t
ON CONFLICT (id) DO NOTHING;

INSERT INTO certificate_document_format (
    id,
    tenant_id,
    certificate_type_id,
    display_name,
    format_kind,
    document_title,
    body_html,
    footer_note,
    internal_note,
    is_active,
    created_at,
    updated_at
)
SELECT
    'f1000000-0000-4000-8000-000000000002'::uuid,
    t.id,
    (SELECT id FROM certificate_type WHERE is_active = true ORDER BY sort_order, name_mr LIMIT 1 OFFSET 1),
    'डमी — दुसरा प्रारूप (दुसरा दाखला प्रकार किंवा लिंकशिवाय)',
    'DAKHALA',
    'नमुना प्रमाणपत्र',
    '<p>{$header}</p>{$title}<p>&nbsp;</p>'
        || '<p>प्रमाणित केले जाते की [नाव] यांचे [पत्ता] येथील पत्ता बरोबर आहे.</p>'
        || '<p>&nbsp;</p>'
        || '<p>दुरध्वनी: [मोबाईल] | दिनांक: [दिनांक] | दाखला क्र.: [दाखला_क्र]</p>'
        || '<p>&nbsp;</p>'
        || '<p>{$footer}</p>',
    'ही आदेशातील नोंद तात्पुरती आहे.',
    'Seed dummy 2 — uses second certificate type if catalog has ≥2 rows; else certificate_type_id is NULL.',
    true,
    now(),
    now()
FROM (SELECT id FROM tenants ORDER BY tenant_code LIMIT 1) AS t
ON CONFLICT (id) DO NOTHING;
