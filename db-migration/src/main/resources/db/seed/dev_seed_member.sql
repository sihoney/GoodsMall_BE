INSERT INTO member.member (
    member_id, email, password, nickname, phone, address, profile_image_key,
    role, status, created_at, updated_at
)
VALUES
    (
        '11111111-1111-1111-1111-111111111101',
        'buyer@test.local',
        '$2a$10$uKM7xOvxJrjvB0JGAKEhwOMFsTjH39xmqUWx44RksLwV/K8AnaDbO',
        'test-buyer',
        '010-1111-1111',
        'Seoul Buyer District',
        NULL,
        'USER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '22222222-2222-2222-2222-222222222202',
        'seller@test.local',
        '$2a$10$uKM7xOvxJrjvB0JGAKEhwOMFsTjH39xmqUWx44RksLwV/K8AnaDbO',
        'test-seller',
        '010-2222-2222',
        'Seoul Seller District',
        NULL,
        'SELLER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '33333333-3333-3333-3333-333333333303',
        'admin@test.local',
        '$2a$10$uKM7xOvxJrjvB0JGAKEhwOMFsTjH39xmqUWx44RksLwV/K8AnaDbO',
        'test-admin',
        '010-3333-3333',
        'Seoul Admin District',
        NULL,
        'ADMIN',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO member.seller (
    seller_id, member_id, bank_name, account, approved_at
)
VALUES (
    '44444444-4444-4444-4444-444444444404',
    '22222222-2222-2222-2222-222222222202',
    'Today Bank',
    '123-456-7890',
    NOW()
)
ON CONFLICT (member_id) DO NOTHING;
