-- ============================================
-- 배포 기본 Seed Data - 회원/판매자/지갑
-- ============================================
-- 기존 운영/개발 데이터는 삭제하지 않고, 없을 때만 추가합니다.
-- 회원이 추가되면 관련 payment.wallet 도 함께 보장합니다.
-- 임시 비밀번호(평문): 1111
-- ============================================

INSERT INTO member.member (
    member_id,
    email,
    password,
    nickname,
    phone,
    address,
    profile_image_key,
    role,
    status,
    created_at,
    updated_at
)
VALUES
    (
        '91000000-0000-0000-0000-000000000001',
        'minji.customer@seed.todaylunch.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '김민지',
        '010-4201-1111',
        '서울시 성동구 왕십리로 83',
        NULL,
        'USER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000002',
        'junho.customer@seed.todaylunch.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '박준호',
        '010-4201-2222',
        '경기도 성남시 분당구 판교역로 166',
        NULL,
        'USER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000003',
        'haneul.seller@seed.todaylunch.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '송하늘',
        '010-4201-3333',
        '서울시 마포구 양화로 188',
        NULL,
        'SELLER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000004',
        'seoyun.seller@seed.todaylunch.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '최서윤',
        '010-4201-4444',
        '부산시 해운대구 센텀중앙로 97',
        NULL,
        'SELLER',
        'ACTIVE',
        NOW(),
        NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000005',
        'admin.seed@seed.todaylunch.local',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '운영관리자',
        '010-4201-9999',
        '서울시 중구 세종대로 110',
        NULL,
        'ADMIN',
        'ACTIVE',
        NOW(),
        NOW()
    )
ON CONFLICT (email) DO NOTHING;

INSERT INTO member.seller (
    seller_id,
    member_id,
    bank_name,
    account,
    approved_at
)
SELECT
    seed.seller_id::UUID,
    member_row.member_id,
    seed.bank_name,
    seed.account,
    NOW()
FROM (
    VALUES
        (
            '92000000-0000-0000-0000-000000000001',
            'haneul.seller@seed.todaylunch.local',
            '국민은행',
            '111-222-333333'
        ),
        (
            '92000000-0000-0000-0000-000000000002',
            'seoyun.seller@seed.todaylunch.local',
            '신한은행',
            '444-555-666666'
        )
) AS seed(seller_id, email, bank_name, account)
JOIN member.member member_row
    ON member_row.email = seed.email
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO payment.wallet (
    wallet_id,
    member_id,
    balance,
    created_at,
    updated_at
)
SELECT
    seed.wallet_id::UUID,
    member_row.member_id,
    seed.balance::DECIMAL(19, 2),
    NOW(),
    NOW()
FROM (
    VALUES
        (
            '93000000-0000-0000-0000-000000000001',
            'minji.customer@seed.todaylunch.local',
            120000.00
        ),
        (
            '93000000-0000-0000-0000-000000000002',
            'junho.customer@seed.todaylunch.local',
            65000.00
        ),
        (
            '93000000-0000-0000-0000-000000000003',
            'haneul.seller@seed.todaylunch.local',
            0.00
        ),
        (
            '93000000-0000-0000-0000-000000000004',
            'seoyun.seller@seed.todaylunch.local',
            0.00
        )
) AS seed(wallet_id, email, balance)
JOIN member.member member_row
    ON member_row.email = seed.email
ON CONFLICT (member_id) DO NOTHING;

-- 운영에 이미 있는 회원도 wallet 누락 시 1:1 관계를 보장합니다.
WITH members_without_wallet AS (
    SELECT
        member_row.member_id,
        (
            SUBSTRING(MD5(member_row.member_id::TEXT || ':wallet'), 1, 8)
            || '-'
            || SUBSTRING(MD5(member_row.member_id::TEXT || ':wallet'), 9, 4)
            || '-'
            || SUBSTRING(MD5(member_row.member_id::TEXT || ':wallet'), 13, 4)
            || '-'
            || SUBSTRING(MD5(member_row.member_id::TEXT || ':wallet'), 17, 4)
            || '-'
            || SUBSTRING(MD5(member_row.member_id::TEXT || ':wallet'), 21, 12)
        )::UUID AS wallet_id
    FROM member.member member_row
    LEFT JOIN payment.wallet wallet_row
        ON wallet_row.member_id = member_row.member_id
    WHERE wallet_row.member_id IS NULL
)
INSERT INTO payment.wallet (
    wallet_id,
    member_id,
    balance,
    created_at,
    updated_at
)
SELECT
    missing_wallet.wallet_id,
    missing_wallet.member_id,
    0.00::DECIMAL(19, 2),
    NOW(),
    NOW()
FROM members_without_wallet missing_wallet
LEFT JOIN payment.wallet wallet_by_id
    ON wallet_by_id.wallet_id = missing_wallet.wallet_id
WHERE wallet_by_id.wallet_id IS NULL
ON CONFLICT (member_id) DO NOTHING;




