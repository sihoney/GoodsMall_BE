-- ============================================
-- Member Seed Data (개발용)
-- ============================================
-- 비밀번호 (평문 → bcrypt):
--   buyer  : 1111
--   seller : 2222
--   admin  : 3333
-- ============================================

INSERT INTO member.member (
    member_id, email, password, nickname, phone, address, profile_image_key,
    role, status, created_at, updated_at
)
VALUES
    (
        '11111111-1111-1111-1111-111111111101',
        'buyer@test.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '김구매',
        '010-1111-1111',
        '서울시 강남구 테헤란로 1',
        NULL,
        'USER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '22222222-2222-2222-2222-222222222202',
        'seller@test.local',
        '$2b$10$2WSJsxM.EYyBp0WC7Of5hehZDI18sL897M0SAsuq5yCR06IgsX7Jy',
        '이판매',
        '010-2222-2222',
        '서울시 마포구 월드컵로 22',
        NULL,
        'SELLER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '33333333-3333-3333-3333-333333333303',
        'admin@test.local',
        '$2b$10$RHZ5.XyK21GALXv5rNPzr.PvcnZoex66O2z7SfPN2DZS6iIr53TXC',
        '관리자',
        '010-3333-3333',
        '서울시 종로구 세종대로 100',
        NULL,
        'ADMIN',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '11111111-1111-1111-1111-111111111102',
        'buyer2@test.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '박입찰',
        '010-1111-1112',
        '서울시 서초구 강남대로 2',
        NULL,
        'USER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '11111111-1111-1111-1111-111111111103',
        'buyer3@test.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '최경매',
        '010-1111-1113',
        '서울시 송파구 올림픽로 3',
        NULL,
        'USER',
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
    '국민은행',
    '123-456-7890',
    NOW()
)
ON CONFLICT (member_id) DO NOTHING;
