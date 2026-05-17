-- Dev seed for master.super_admins.
-- Sign in: mobile 9876543210 / password Platform@123

INSERT INTO super_admins (
    id,
    mobile,
    password_hash,
    name,
    is_active,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    '9876543210',
    'Platform@123',
    'Platform Administrator',
    true,
    now(),
    now()
)
ON CONFLICT (mobile) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    name = EXCLUDED.name,
    is_active = EXCLUDED.is_active,
    updated_at = now();
