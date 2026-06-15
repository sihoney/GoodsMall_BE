-- ============================================================
-- 베이스라인 테스트 전용 입찰자 시드
-- 실행 시점: baseline.js 최초 실행 전 1회
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -n goods-mall -- \
--     psql -U postgres -d goods_mall -f /path/to/seed_baseline_wallets.sql
--
-- 입찰자 30명: member_id 11111111-1111-1111-1111-11111111111[0-9],
--             11111111-1111-1111-1111-11111111112[0-9],
--             11111111-1111-1111-1111-11111111113[0-9]
-- 풀 크기 = 최대 VU 수(30) → VU마다 전담 입찰자, 동일 입찰자 동시 사용 없음
-- 초기 잔액: 5,000,000 (5분 테스트 중 경매가 상승을 고려한 여유 금액)
-- 잔액 소진 시: refill_wallet.sql 실행
-- 테스트 완료 후: cleanup_test_data.sql 의 BASELINE STEP 에서 row 삭제
-- ============================================================

-- ① member 레코드 생성 (payment 서비스 member 검증 통과를 위해 필요)
INSERT INTO member.member (member_id, email, password, nickname, role, status, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111110', 'baseline-bidder-01@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자01', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111111', 'baseline-bidder-02@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자02', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111112', 'baseline-bidder-03@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자03', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111113', 'baseline-bidder-04@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자04', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111114', 'baseline-bidder-05@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자05', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111115', 'baseline-bidder-06@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자06', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111116', 'baseline-bidder-07@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자07', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111117', 'baseline-bidder-08@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자08', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111118', 'baseline-bidder-09@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자09', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111119', 'baseline-bidder-10@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자10', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111120', 'baseline-bidder-11@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자11', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111121', 'baseline-bidder-12@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자12', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111122', 'baseline-bidder-13@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자13', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111123', 'baseline-bidder-14@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자14', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111124', 'baseline-bidder-15@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자15', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111125', 'baseline-bidder-16@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자16', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111126', 'baseline-bidder-17@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자17', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111127', 'baseline-bidder-18@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자18', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111128', 'baseline-bidder-19@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자19', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111129', 'baseline-bidder-20@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자20', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111130', 'baseline-bidder-21@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자21', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111131', 'baseline-bidder-22@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자22', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111132', 'baseline-bidder-23@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자23', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111133', 'baseline-bidder-24@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자24', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111134', 'baseline-bidder-25@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자25', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111135', 'baseline-bidder-26@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자26', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111136', 'baseline-bidder-27@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자27', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111137', 'baseline-bidder-28@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자28', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111138', 'baseline-bidder-29@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자29', 'USER', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111139', 'baseline-bidder-30@test.local', '$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u', '베이스라인입찰자30', 'USER', 'ACTIVE', NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

-- ② wallet 생성
INSERT INTO payment.wallet (wallet_id, member_id, balance, created_at, updated_at)
VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccc10', '11111111-1111-1111-1111-111111111110', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc11', '11111111-1111-1111-1111-111111111111', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc12', '11111111-1111-1111-1111-111111111112', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc13', '11111111-1111-1111-1111-111111111113', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc14', '11111111-1111-1111-1111-111111111114', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc15', '11111111-1111-1111-1111-111111111115', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc16', '11111111-1111-1111-1111-111111111116', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc17', '11111111-1111-1111-1111-111111111117', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc18', '11111111-1111-1111-1111-111111111118', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc19', '11111111-1111-1111-1111-111111111119', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc20', '11111111-1111-1111-1111-111111111120', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc21', '11111111-1111-1111-1111-111111111121', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc22', '11111111-1111-1111-1111-111111111122', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc23', '11111111-1111-1111-1111-111111111123', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc24', '11111111-1111-1111-1111-111111111124', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc25', '11111111-1111-1111-1111-111111111125', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc26', '11111111-1111-1111-1111-111111111126', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc27', '11111111-1111-1111-1111-111111111127', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc28', '11111111-1111-1111-1111-111111111128', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc29', '11111111-1111-1111-1111-111111111129', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc30', '11111111-1111-1111-1111-111111111130', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc31', '11111111-1111-1111-1111-111111111131', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc32', '11111111-1111-1111-1111-111111111132', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc33', '11111111-1111-1111-1111-111111111133', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc34', '11111111-1111-1111-1111-111111111134', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc35', '11111111-1111-1111-1111-111111111135', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc36', '11111111-1111-1111-1111-111111111136', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc37', '11111111-1111-1111-1111-111111111137', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc38', '11111111-1111-1111-1111-111111111138', 5000000.00, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-cccccccccc39', '11111111-1111-1111-1111-111111111139', 5000000.00, NOW(), NOW())
ON CONFLICT (member_id) DO UPDATE
    SET balance    = 5000000.00,
        updated_at = NOW();

-- 생성 확인
SELECT wallet_id, member_id, balance
FROM payment.wallet
WHERE member_id IN (
    '11111111-1111-1111-1111-111111111110',
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111112',
    '11111111-1111-1111-1111-111111111113',
    '11111111-1111-1111-1111-111111111114',
    '11111111-1111-1111-1111-111111111115',
    '11111111-1111-1111-1111-111111111116',
    '11111111-1111-1111-1111-111111111117',
    '11111111-1111-1111-1111-111111111118',
    '11111111-1111-1111-1111-111111111119',
    '11111111-1111-1111-1111-111111111120',
    '11111111-1111-1111-1111-111111111121',
    '11111111-1111-1111-1111-111111111122',
    '11111111-1111-1111-1111-111111111123',
    '11111111-1111-1111-1111-111111111124',
    '11111111-1111-1111-1111-111111111125',
    '11111111-1111-1111-1111-111111111126',
    '11111111-1111-1111-1111-111111111127',
    '11111111-1111-1111-1111-111111111128',
    '11111111-1111-1111-1111-111111111129',
    '11111111-1111-1111-1111-111111111130',
    '11111111-1111-1111-1111-111111111131',
    '11111111-1111-1111-1111-111111111132',
    '11111111-1111-1111-1111-111111111133',
    '11111111-1111-1111-1111-111111111134',
    '11111111-1111-1111-1111-111111111135',
    '11111111-1111-1111-1111-111111111136',
    '11111111-1111-1111-1111-111111111137',
    '11111111-1111-1111-1111-111111111138',
    '11111111-1111-1111-1111-111111111139'
)
ORDER BY member_id;
