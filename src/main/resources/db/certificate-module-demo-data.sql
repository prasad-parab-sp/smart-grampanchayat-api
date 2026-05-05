-- =============================================================================
-- Demo seed: platform certificate types + dynamic fields (MR + EN) + hints + per-tenant
-- certificate config (PostgreSQL).
-- Run after certificate-module-form-only.sql (creates tenant_certificate_type_config).
-- Idempotent: safe to re-run; skips existing rows via NOT EXISTS / ON CONFLICT.
-- =============================================================================

-- ── Platform certificate types (tenant_id IS NULL) ───────────────────────────

INSERT INTO certificate_type (
    tenant_id, code, category,
    name_mr, name_en,
    description_mr, description_en,
    extra_fields_section_title_mr, extra_fields_section_title_en,
    default_fee_amount, estimated_days_txt, icon, sort_order
)
SELECT NULL, 'JANMA_DAKHALA', 'CERTIFICATE',
    'जन्म दाखला', 'Birth certificate',
    'जन्म नोंदणी प्रमाणपत्रासाठी आवश्यक माहिती.', 'Details required for the birth certificate.',
    'जन्म दाखल्यासाठी अतिरिक्त माहिती', 'Additional details (birth)',
    20.00, '१-२ दिवस', '👶', 10
WHERE NOT EXISTS (SELECT 1 FROM certificate_type c WHERE c.code = 'JANMA_DAKHALA' AND c.tenant_id IS NULL);

INSERT INTO certificate_type (
    tenant_id, code, category,
    name_mr, name_en,
    description_mr, description_en,
    extra_fields_section_title_mr, extra_fields_section_title_en,
    default_fee_amount, estimated_days_txt, icon, sort_order
)
SELECT NULL, 'VIVAH_DAKHALA', 'CERTIFICATE',
    'विवाह दाखला', 'Marriage certificate',
    'विवाह नोंदणी प्रमाणपत्रासाठी आवश्यक माहिती.', 'Details required for the marriage certificate.',
    'विवाह दाखल्यासाठी अतिरिक्त माहिती', 'Additional details (marriage)',
    20.00, '२-३ दिवस', '💍', 20
WHERE NOT EXISTS (SELECT 1 FROM certificate_type c WHERE c.code = 'VIVAH_DAKHALA' AND c.tenant_id IS NULL);

INSERT INTO certificate_type (
    tenant_id, code, category,
    name_mr, name_en,
    description_mr, description_en,
    extra_fields_section_title_mr, extra_fields_section_title_en,
    default_fee_amount, estimated_days_txt, icon, sort_order
)
SELECT NULL, 'UTPADNA_DAKHALA', 'CERTIFICATE',
    'उत्पन्न दाखला', 'Income certificate',
    'वार्षिक उत्पन्न दाखल्यासाठी आवश्यक माहिती व कागदपत्रे.', 'Information and documents for income certificate.',
    'उत्पन्न दाखल्यासाठी अतिरिक्त माहिती', 'Additional details (income)',
    20.00, '३-४ दिवस', '💵', 30
WHERE NOT EXISTS (SELECT 1 FROM certificate_type c WHERE c.code = 'UTPADNA_DAKHALA' AND c.tenant_id IS NULL);

INSERT INTO certificate_type (
    tenant_id, code, category,
    name_mr, name_en,
    description_mr, description_en,
    extra_fields_section_title_mr, extra_fields_section_title_en,
    default_fee_amount, estimated_days_txt, icon, sort_order
)
SELECT NULL, 'RAHIVAS_DAKHALA', 'CERTIFICATE',
    'रहिवास दाखला', 'Residence certificate',
    'गावात कायमचे रहिवास असल्याचा दाखला.', 'Certificate of permanent residence in the village.',
    'रहिवास दाखल्यासाठी अतिरिक्त माहिती', 'Additional details (residence)',
    20.00, '२-३ दिवस', '🏠', 40
WHERE NOT EXISTS (SELECT 1 FROM certificate_type c WHERE c.code = 'RAHIVAS_DAKHALA' AND c.tenant_id IS NULL);

INSERT INTO certificate_type (
    tenant_id, code, category,
    name_mr, name_en,
    description_mr, description_en,
    extra_fields_section_title_mr, extra_fields_section_title_en,
    default_fee_amount, estimated_days_txt, icon, sort_order
)
SELECT NULL, 'MRUTYU_DAKHALA', 'CERTIFICATE',
    'मृत्यु दाखला', 'Death certificate',
    'मृत्यु नोंदणी प्रमाणपत्रासाठी आवश्यक माहिती.', 'Details required for the death certificate.',
    'मृत्यु दाखल्यासाठी अतिरिक्त माहिती', 'Additional details (death)',
    25.00, '२-३ दिवस', '🕊', 15
WHERE NOT EXISTS (SELECT 1 FROM certificate_type c WHERE c.code = 'MRUTYU_DAKHALA' AND c.tenant_id IS NULL);

-- Backfill icons for demo platform types created before this column existed
UPDATE certificate_type SET icon = CASE code
    WHEN 'JANMA_DAKHALA' THEN '👶'
    WHEN 'VIVAH_DAKHALA' THEN '💍'
    WHEN 'UTPADNA_DAKHALA' THEN '💵'
    WHEN 'RAHIVAS_DAKHALA' THEN '🏠'
    WHEN 'MRUTYU_DAKHALA' THEN '🕊'
    ELSE icon
END
WHERE tenant_id IS NULL
  AND code IN ('JANMA_DAKHALA', 'VIVAH_DAKHALA', 'UTPADNA_DAKHALA', 'RAHIVAS_DAKHALA', 'MRUTYU_DAKHALA');


-- ── Extra fields (certificate_type_field) ───────────────────────────────────

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'subject_person_name_mr', 'कोणाच्या जन्माचा दाखला — त्याचे/तिचे नाव', 'पूर्ण नाव', 'TEXT', TRUE, 10
FROM certificate_type ct WHERE ct.code = 'CERT_BIRTH' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'child_name_mr', 'बाळाचे नाव (असल्यास)', 'पूर्ण नाव', 'TEXT', FALSE, 20
FROM certificate_type ct WHERE ct.code = 'JANMA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order
)
SELECT ct.id, 'date_of_birth', 'जन्म तारीख', 'DATE', TRUE, 30
FROM certificate_type ct WHERE ct.code = 'JANMA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'place_of_birth_mr', 'जन्म ठिकाण', 'उदा. जिल्हा रुग्णालय', 'TEXT', FALSE, 40
FROM certificate_type ct WHERE ct.code = 'JANMA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;


INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order,
    options_json
)
SELECT ct.id, 'certificate_in_name_of', 'कोणाच्या नावाने दाखला पाहिजे', 'SELECT', TRUE, 10,
    '[{"value":"GROOM","label_mr":"नवरा (वर)"},{"value":"BRIDE","label_mr":"बायको (वधू)"},{"value":"BOTH","label_mr":"दोघांच्या नावाने"}]'::jsonb
FROM certificate_type ct WHERE ct.code = 'VIVAH_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'groom_full_name_mr', 'नवऱ्याचे (वराचे) नाव', 'वराचे पूर्ण नाव', 'TEXT', TRUE, 20
FROM certificate_type ct WHERE ct.code = 'VIVAH_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'bride_full_name_mr', 'बायकोचे (वधूचे) नाव', 'वधूचे पूर्ण नाव', 'TEXT', TRUE, 30
FROM certificate_type ct WHERE ct.code = 'VIVAH_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order
)
SELECT ct.id, 'marriage_date', 'विवाह तारीख', 'DATE', TRUE, 40
FROM certificate_type ct WHERE ct.code = 'VIVAH_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order,
    max_files, allowed_mime_csv
)
SELECT ct.id, 'invitation_card', 'लग्नपत्रिका / निमंत्रण', 'फोटो किंवा PDF', 'FILE', FALSE, 50,
    1, 'image/jpeg,image/png,application/pdf'
FROM certificate_type ct WHERE ct.code = 'VIVAH_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;


INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'income_subject_name_mr', 'कोणाच्या नावाचा उत्पन्न दाखला', 'अर्जदाराचे पूर्ण नाव', 'TEXT', TRUE, 10
FROM certificate_type ct WHERE ct.code = 'UTPADNA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order,
    max_files, allowed_mime_csv
)
SELECT ct.id, 'aadhar_scan', 'आधार कार्ड', 'FILE', TRUE, 20, 1, 'image/jpeg,image/png,application/pdf'
FROM certificate_type ct WHERE ct.code = 'UTPADNA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order,
    max_files, allowed_mime_csv
)
SELECT ct.id, 'income_proof', 'उत्पन्न पुरावा', 'FILE', TRUE, 30, 2, 'image/jpeg,image/png,application/pdf'
FROM certificate_type ct WHERE ct.code = 'UTPADNA_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;


INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'residence_subject_name_mr', 'कोणाच्या नावाचा रहिवास दाखला', 'पूर्ण नाव', 'TEXT', TRUE, 10
FROM certificate_type ct WHERE ct.code = 'RAHIVAS_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;


-- मृत्यु दाखला — differs from जन्म (birth): deceased identity, dates, optional medical doc
INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'deceased_full_name_mr', 'मृत व्यक्तीचे पूर्ण नाव', 'नाव टाका', 'TEXT', TRUE, 10
FROM certificate_type ct WHERE ct.code = 'MRUTYU_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order
)
SELECT ct.id, 'deceased_date_of_birth', 'मृत व्यक्तीची जन्म तारीख', 'DATE', FALSE, 20
FROM certificate_type ct WHERE ct.code = 'MRUTYU_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, data_type, required, sort_order
)
SELECT ct.id, 'date_of_death', 'मृत्यूची तारीख', 'DATE', TRUE, 30
FROM certificate_type ct WHERE ct.code = 'MRUTYU_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order
)
SELECT ct.id, 'place_of_death_mr', 'मृत्यूचे ठिकाण', 'उदा. रुग्णालय / गाव — वस्ती', 'TEXTAREA', FALSE, 40
FROM certificate_type ct WHERE ct.code = 'MRUTYU_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;

INSERT INTO certificate_type_field (
    certificate_type_id, field_key, label_mr, placeholder_mr, data_type, required, sort_order,
    max_files, allowed_mime_csv
)
SELECT ct.id, 'hospital_discharge_death_doc', 'रुग्णालय दाखला / डॉक्टर प्रमाणपत्र (आहे तर)', 'PDF किंवा फोटो', 'FILE', FALSE, 50,
    1, 'image/jpeg,image/png,application/pdf'
FROM certificate_type ct WHERE ct.code = 'MRUTYU_DAKHALA' AND ct.tenant_id IS NULL
ON CONFLICT (certificate_type_id, field_key) DO NOTHING;


-- ── English copy for dynamic fields (label_en, placeholder_en). No SELECT/FROM. ──
-- Requires certificate-type-field-en-columns.sql (or fresh DDL). Rows matched by field_key only.

UPDATE certificate_type_field
SET label_en = CASE field_key
        WHEN 'subject_person_name_mr' THEN 'Birth certificate is for whose birth — full name'
        WHEN 'child_name_mr' THEN 'Child''s name (if applicable)'
        WHEN 'date_of_birth' THEN 'Date of birth'
        WHEN 'place_of_birth_mr' THEN 'Place of birth'
        WHEN 'income_subject_name_mr' THEN 'Income certificate in whose name?'
        WHEN 'aadhar_scan' THEN 'Aadhaar card'
        WHEN 'income_proof' THEN 'Income proof'
        WHEN 'residence_subject_name_mr' THEN 'Residence certificate in whose name?'
        WHEN 'deceased_full_name_mr' THEN 'Deceased person''s full name'
        WHEN 'deceased_date_of_birth' THEN 'Date of birth (deceased)'
        WHEN 'date_of_death' THEN 'Date of death'
        WHEN 'place_of_death_mr' THEN 'Place of death'
        WHEN 'hospital_discharge_death_doc' THEN 'Hospital discharge / doctor certificate (if any)'
    END,
    placeholder_en = CASE field_key
        WHEN 'subject_person_name_mr' THEN 'Full name'
        WHEN 'child_name_mr' THEN 'Full name'
        WHEN 'place_of_birth_mr' THEN 'e.g. district hospital'
        WHEN 'income_subject_name_mr' THEN 'Applicant''s full name'
        WHEN 'residence_subject_name_mr' THEN 'Full name'
        WHEN 'deceased_full_name_mr' THEN 'Enter full name'
        WHEN 'place_of_death_mr' THEN 'e.g. hospital / village — locality'
        WHEN 'hospital_discharge_death_doc' THEN 'PDF or photo'
        ELSE placeholder_en
    END
WHERE field_key IN (
    'subject_person_name_mr','child_name_mr','date_of_birth','place_of_birth_mr',
    'income_subject_name_mr','aadhar_scan','income_proof','residence_subject_name_mr',
    'deceased_full_name_mr','deceased_date_of_birth','date_of_death','place_of_death_mr',
    'hospital_discharge_death_doc'
);

UPDATE certificate_type_field
SET label_en = CASE field_key
        WHEN 'groom_full_name_mr' THEN 'Groom''s full name'
        WHEN 'bride_full_name_mr' THEN 'Bride''s full name'
        WHEN 'marriage_date' THEN 'Marriage date'
        WHEN 'invitation_card' THEN 'Wedding invitation / card'
    END,
    placeholder_en = CASE field_key
        WHEN 'groom_full_name_mr' THEN 'Groom''s full name'
        WHEN 'bride_full_name_mr' THEN 'Bride''s full name'
        WHEN 'invitation_card' THEN 'Photo or PDF'
        ELSE placeholder_en
    END
WHERE field_key IN ('groom_full_name_mr','bride_full_name_mr','marriage_date','invitation_card');

UPDATE certificate_type_field
SET label_en = 'Certificate should be issued in whose name?',
    options_json = '[{"value":"GROOM","label_mr":"नवरा (वर)","label_en":"Groom"},{"value":"BRIDE","label_mr":"बायको (वधू)","label_en":"Bride"},{"value":"BOTH","label_mr":"दोघांच्या नावाने","label_en":"Both"}]'::jsonb
WHERE field_key = 'certificate_in_name_of';


-- ── Document hint bullets — उत्पन्न दाखला ───────────────────────────────────

INSERT INTO certificate_type_document_hint (certificate_type_id, sort_order, hint_text_mr)
SELECT ct.id, 10, 'आधार कार्ड (स्वतःचे)'
FROM certificate_type ct WHERE ct.code = 'UTPADNA_DAKHALA' AND ct.tenant_id IS NULL
AND NOT EXISTS (
    SELECT 1 FROM certificate_type_document_hint h
    WHERE h.certificate_type_id = ct.id AND h.sort_order = 10
);

INSERT INTO certificate_type_document_hint (certificate_type_id, sort_order, hint_text_mr)
SELECT ct.id, 20, 'शेतजमीन असल्यास ७/१२ उतारा (पर्यायी)'
FROM certificate_type ct WHERE ct.code = 'UTPADNA_DAKHALA' AND ct.tenant_id IS NULL
AND NOT EXISTS (
    SELECT 1 FROM certificate_type_document_hint h
    WHERE h.certificate_type_id = ct.id AND h.sort_order = 20
);


-- ── Per-tenant config (platform types: fee + is_enabled) ───────────────────
-- Picks one tenant row from shard `tenants` by code. Change 'GP001' if yours differs.
-- Certificate types are matched by code so UUIDs need not match your DB.
-- Idempotency uses NOT EXISTS (works even if unique index below was not applied yet).
-- For ON CONFLICT upserts you need:
--   CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_cert_config_tenant_type
--     ON tenant_certificate_type_config (tenant_id, certificate_type_id);

INSERT INTO tenant_certificate_type_config (
    tenant_id, certificate_type_id, fee_amount, is_enabled
)
SELECT t.id, ct.id, ct.default_fee_amount, TRUE
FROM tenants t
INNER JOIN certificate_type ct
    ON ct.tenant_id IS NULL
    AND ct.code IN ('JANMA_DAKHALA', 'VIVAH_DAKHALA', 'UTPADNA_DAKHALA', 'MRUTYU_DAKHALA')
WHERE t.tenant_code = 'GP001'
  AND NOT EXISTS (
      SELECT 1
      FROM tenant_certificate_type_config cfg
      WHERE cfg.tenant_id = t.id
        AND cfg.certificate_type_id = ct.id
  );


-- ── Same as above, but bound to known certificate_type.id values (e.g. after a manual seed).
-- Idempotent. Change tenant_code if not GP001.

INSERT INTO tenant_certificate_type_config (tenant_id, certificate_type_id, fee_amount, is_enabled)
SELECT t.id, ct.id, ct.default_fee_amount, TRUE
FROM tenants t
INNER JOIN certificate_type ct ON ct.id IN (
    '62539cb0-a132-4a0a-a838-bd990cbf1621'::uuid,
    '625f6de6-fd33-4407-b9e6-89243887d731'::uuid,
    '2ed866d2-6cad-494f-aa0b-dc8a083c5bc8'::uuid
)
WHERE t.tenant_code = 'GP001'
  AND NOT EXISTS (
      SELECT 1
      FROM tenant_certificate_type_config cfg
      WHERE cfg.tenant_id = t.id
        AND cfg.certificate_type_id = ct.id
  );


-- ── Optional: literal VALUES when you already know tenants.id (replace tenant UUID) ─

-- INSERT INTO tenant_certificate_type_config (tenant_id, certificate_type_id, fee_amount, is_enabled)
-- SELECT v.tenant_id, v.certificate_type_id, v.fee_amount, v.is_enabled
-- FROM (VALUES
--   ('11111111-2222-3333-4444-555555555555'::uuid, '62539cb0-a132-4a0a-a838-bd990cbf1621'::uuid, 20.00::numeric, TRUE),
--   ('11111111-2222-3333-4444-555555555555'::uuid, '625f6de6-fd33-4407-b9e6-89243887d731'::uuid, 20.00::numeric, TRUE),
--   ('11111111-2222-3333-4444-555555555555'::uuid, '2ed866d2-6cad-494f-aa0b-dc8a083c5bc8'::uuid, 20.00::numeric, TRUE)
-- ) AS v(tenant_id, certificate_type_id, fee_amount, is_enabled)
-- WHERE NOT EXISTS (
--   SELECT 1 FROM tenant_certificate_type_config c
--   WHERE c.tenant_id = v.tenant_id AND c.certificate_type_id = v.certificate_type_id
-- );
