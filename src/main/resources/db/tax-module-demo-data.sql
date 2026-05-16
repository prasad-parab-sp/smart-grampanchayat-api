-- Demo tax data for district shard (idempotent).
-- Targets tenant_code 'adali' when present.

DO $$
DECLARE
    v_tenant_id   UUID;
    v_gramsevak   UUID;
    v_property    UUID := 'e1000001-0001-4001-8001-000000000001';
    v_water       UUID := 'e1000001-0001-4001-8001-000000000002';
    v_garbage     UUID := 'e1000001-0001-4001-8001-000000000003';
    v_now         TIMESTAMPTZ := now();
BEGIN
    SELECT id INTO v_tenant_id FROM tenants WHERE tenant_code = 'adali' LIMIT 1;
    IF v_tenant_id IS NULL THEN
        RAISE NOTICE 'No adali tenant; skipping tax demo data.';
        RETURN;
    END IF;

    SELECT id INTO v_gramsevak
    FROM users
    WHERE tenant_id = v_tenant_id AND role::text = 'GRAMSEVAK'
    LIMIT 1;
    IF v_gramsevak IS NULL THEN
        SELECT id INTO v_gramsevak
        FROM users
        WHERE tenant_id = v_tenant_id AND role::text IN ('GP_ADMIN', 'OPERATOR', 'OPERTAOR')
        LIMIT 1;
    END IF;

    INSERT INTO tax_type (id, tenant_id, name_en, name_mr, description, is_active, created_at, updated_at)
    VALUES
        (v_property, v_tenant_id, 'Property Tax', 'मालमत्ता कर',
         'Annual property tax for residential and commercial plots.', TRUE, v_now, v_now),
        (v_water, v_tenant_id, 'Water Tax', 'पाणी कर',
         'Water supply and drainage charges.', TRUE, v_now, v_now),
        (v_garbage, v_tenant_id, 'Garbage Collection Fee', 'कचरा संकलन शुल्क',
         'Custom village sanitation fee.', TRUE, v_now, v_now)
    ON CONFLICT (id) DO NOTHING;

    -- Shivraj Mane: property tax pending
    INSERT INTO citizen_tax (
        id, tenant_id, citizen_id, tax_type_id, financial_year, assessment_number,
        amount_assessed, amount_outstanding, due_date, status, remarks,
        created_by_user_id, created_at, updated_at
    )
    VALUES (
        'e2000001-0001-4002-8002-000000000001', v_tenant_id,
        'c0000000-0000-0000-0000-000000000001', v_property, '2025-26', 'PT-2025-001',
        2500.00, 2500.00, '2026-03-31', 'PENDING', 'Residential plot — ward 3',
        v_gramsevak, v_now, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    -- Shivraj Mane: water tax partial (500 paid of 800)
    INSERT INTO citizen_tax (
        id, tenant_id, citizen_id, tax_type_id, financial_year, assessment_number,
        amount_assessed, amount_outstanding, due_date, status, remarks,
        created_by_user_id, created_at, updated_at
    )
    VALUES (
        'e2000001-0001-4002-8002-000000000002', v_tenant_id,
        'c0000000-0000-0000-0000-000000000001', v_water, '2025-26', 'WT-2025-001',
        800.00, 300.00, '2026-06-30', 'PARTIAL', 'Domestic connection',
        v_gramsevak, v_now, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO tax_payment (
        id, tenant_id, citizen_tax_id, amount, paid_on, payment_mode,
        receipt_number, reference, recorded_by_user_id, created_at
    )
    VALUES (
        'e3000001-0001-4003-8003-000000000001', v_tenant_id,
        'e2000001-0001-4002-8002-000000000002', 500.00, '2026-01-15', 'UPI',
        'RCP-20260115-482901', 'UPI@ybl/482901', v_gramsevak, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    -- Sangita Mane: property tax fully paid
    INSERT INTO citizen_tax (
        id, tenant_id, citizen_id, tax_type_id, financial_year, assessment_number,
        amount_assessed, amount_outstanding, due_date, status, remarks,
        created_by_user_id, created_at, updated_at
    )
    VALUES (
        'e2000001-0001-4002-8002-000000000003', v_tenant_id,
        'c0000000-0000-0000-0000-000000000002', v_property, '2025-26', 'PT-2025-002',
        1800.00, 0.00, '2026-03-31', 'PAID', NULL,
        v_gramsevak, v_now, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    INSERT INTO tax_payment (
        id, tenant_id, citizen_tax_id, amount, paid_on, payment_mode,
        receipt_number, reference, recorded_by_user_id, created_at
    )
    VALUES (
        'e3000001-0001-4003-8003-000000000002', v_tenant_id,
        'e2000001-0001-4002-8002-000000000003', 1800.00, '2025-11-20', 'CASH',
        'RCP-20251120-173540', NULL, v_gramsevak, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    -- Rohit Mane: garbage fee pending
    INSERT INTO citizen_tax (
        id, tenant_id, citizen_id, tax_type_id, financial_year, assessment_number,
        amount_assessed, amount_outstanding, due_date, status, remarks,
        created_by_user_id, created_at, updated_at
    )
    VALUES (
        'e2000001-0001-4002-8002-000000000004', v_tenant_id,
        'c0000000-0000-0000-0000-000000000003', v_garbage, '2025-26', 'GC-2025-001',
        360.00, 360.00, '2026-12-31', 'PENDING', 'Annual sanitation charge',
        v_gramsevak, v_now, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    -- Lata Pednekar: water tax pending
    INSERT INTO citizen_tax (
        id, tenant_id, citizen_id, tax_type_id, financial_year, assessment_number,
        amount_assessed, amount_outstanding, due_date, status, remarks,
        created_by_user_id, created_at, updated_at
    )
    VALUES (
        'e2000001-0001-4002-8002-000000000005', v_tenant_id,
        'c0000000-0000-0000-0000-000000000004', v_water, '2025-26', 'WT-2025-010',
        650.00, 650.00, '2026-06-30', 'PENDING', NULL,
        v_gramsevak, v_now, v_now
    )
    ON CONFLICT (id) DO NOTHING;

    RAISE NOTICE 'Tax demo data applied for tenant %', v_tenant_id;
END
$$;
