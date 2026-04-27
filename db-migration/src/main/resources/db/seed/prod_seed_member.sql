-- ============================================
-- 배포 기본 Seed Data - 회원/판매자/지갑
-- ============================================
-- 기존 운영/개발 데이터는 삭제하지 않고, 없을 때만 추가합니다.
-- 회원이 추가되면 관련 payment.wallet 도 함께 보장합니다.
-- 비밀번호(평문): 1111
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
    -- 구매자 5명
    (
        '91000000-0000-0000-0000-000000000001',
        'buyer1@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '김민지',
        '010-4201-1001',
        '서울시 성동구 왕십리로 83',
        NULL, 'USER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000002',
        'buyer2@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '박준호',
        '010-4201-1002',
        '경기도 성남시 분당구 판교역로 166',
        NULL, 'USER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000003',
        'buyer3@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '이수진',
        '010-4201-1003',
        '서울시 마포구 홍익로 6',
        NULL, 'USER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000004',
        'buyer4@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '최지우',
        '010-4201-1004',
        '부산시 해운대구 해운대로 772',
        NULL, 'USER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000005',
        'buyer5@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '정다은',
        '010-4201-1005',
        '인천시 연수구 송도대로 123',
        NULL, 'USER', 'ACTIVE', NOW(), NOW()
    ),
    -- 판매자 5명
    (
        '91000000-0000-0000-0000-000000000006',
        'seller1@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '송하늘',
        '010-4201-2001',
        '서울시 마포구 양화로 188',
        NULL, 'SELLER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000007',
        'seller2@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '최서윤',
        '010-4201-2002',
        '부산시 해운대구 센텀중앙로 97',
        NULL, 'SELLER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000008',
        'seller3@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '박굿즈',
        '010-4201-2003',
        '서울시 강남구 테헤란로 427',
        NULL, 'SELLER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000009',
        'seller4@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '이한정',
        '010-4201-2004',
        '대구시 수성구 범어로 150',
        NULL, 'SELLER', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000010',
        'seller5@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '정마켓',
        '010-4201-2005',
        '광주시 북구 첨단과기로 208',
        NULL, 'SELLER', 'ACTIVE', NOW(), NOW()
    ),
    -- 관리자 5명
    (
        '91000000-0000-0000-0000-000000000011',
        'admin1@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '운영관리자',
        '010-4201-9001',
        '서울시 중구 세종대로 110',
        NULL, 'ADMIN', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000012',
        'admin2@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '관리자2',
        '010-4201-9002',
        '서울시 중구 세종대로 110',
        NULL, 'ADMIN', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000013',
        'admin3@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '관리자3',
        '010-4201-9003',
        '서울시 중구 세종대로 110',
        NULL, 'ADMIN', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000014',
        'admin4@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '관리자4',
        '010-4201-9004',
        '서울시 중구 세종대로 110',
        NULL, 'ADMIN', 'ACTIVE', NOW(), NOW()
    ),
    (
        '91000000-0000-0000-0000-000000000015',
        'admin5@todaylunchmenu.com',
        '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u',
        '관리자5',
        '010-4201-9005',
        '서울시 중구 세종대로 110',
        NULL, 'ADMIN', 'ACTIVE', NOW(), NOW()
    )
ON CONFLICT (email) DO NOTHING;

-- 판매자 5명 seller 테이블 등록
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
        ('92000000-0000-0000-0000-000000000001', 'seller1@todaylunchmenu.com', '국민은행',   '111-222-333333'),
        ('92000000-0000-0000-0000-000000000002', 'seller2@todaylunchmenu.com', '신한은행',   '444-555-666666'),
        ('92000000-0000-0000-0000-000000000003', 'seller3@todaylunchmenu.com', '우리은행',   '777-888-999999'),
        ('92000000-0000-0000-0000-000000000004', 'seller4@todaylunchmenu.com', '하나은행',   '000-111-222222'),
        ('92000000-0000-0000-0000-000000000005', 'seller5@todaylunchmenu.com', '카카오뱅크', '333-444-555555')
) AS seed(seller_id, email, bank_name, account)
JOIN member.member member_row ON member_row.email = seed.email
ON CONFLICT (member_id) DO NOTHING;

-- 구매자·판매자 지갑 생성
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
        ('93000000-0000-0000-0000-000000000001', 'buyer1@todaylunchmenu.com',  120000.00),
        ('93000000-0000-0000-0000-000000000002', 'buyer2@todaylunchmenu.com',   65000.00),
        ('93000000-0000-0000-0000-000000000003', 'buyer3@todaylunchmenu.com',   95000.00),
        ('93000000-0000-0000-0000-000000000004', 'buyer4@todaylunchmenu.com',   50000.00),
        ('93000000-0000-0000-0000-000000000005', 'buyer5@todaylunchmenu.com',  200000.00),
        ('93000000-0000-0000-0000-000000000006', 'seller1@todaylunchmenu.com',  500000.00),
        ('93000000-0000-0000-0000-000000000007', 'seller2@todaylunchmenu.com',  500000.00),
        ('93000000-0000-0000-0000-000000000008', 'seller3@todaylunchmenu.com',  500000.00),
        ('93000000-0000-0000-0000-000000000009', 'seller4@todaylunchmenu.com',  500000.00),
        ('93000000-0000-0000-0000-000000000010', 'seller5@todaylunchmenu.com',  500000.00),
        ('93000000-0000-0000-0000-000000000011', 'admin1@todaylunchmenu.com',  1000000.00),
        ('93000000-0000-0000-0000-000000000012', 'admin2@todaylunchmenu.com',  1000000.00),
        ('93000000-0000-0000-0000-000000000013', 'admin3@todaylunchmenu.com',  1000000.00),
        ('93000000-0000-0000-0000-000000000014', 'admin4@todaylunchmenu.com',  1000000.00),
        ('93000000-0000-0000-0000-000000000015', 'admin5@todaylunchmenu.com',  1000000.00)
) AS seed(wallet_id, email, balance)
JOIN member.member member_row ON member_row.email = seed.email
ON CONFLICT (member_id) DO NOTHING;

-- 이미 존재하는 회원 중 wallet 누락 시 1:1 관계를 보장합니다.
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
    LEFT JOIN payment.wallet wallet_row ON wallet_row.member_id = member_row.member_id
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
LEFT JOIN payment.wallet wallet_by_id ON wallet_by_id.wallet_id = missing_wallet.wallet_id
WHERE wallet_by_id.wallet_id IS NULL
ON CONFLICT (member_id) DO NOTHING;
