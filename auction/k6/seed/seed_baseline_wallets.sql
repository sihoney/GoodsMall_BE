-- ============================================================
-- 베이스라인 테스트 전용 입찰자 wallet 시드
-- 실행 시점: baseline.js 최초 실행 전 1회
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -n goods-mall -- \
--     psql -U postgres -d goods_mall -f /path/to/seed_baseline_wallets.sql
--
-- 입찰자 10명: member_id 11111111-1111-1111-1111-11111111111[0-9]
-- 초기 잔액: 5,000,000 (5분 테스트 중 경매가 상승을 고려한 여유 금액)
-- 잔액 소진 시: refill_wallet.sql 실행
-- 테스트 완료 후: cleanup_test_data.sql 의 BASELINE STEP 에서 row 삭제
-- ============================================================

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
    ('cccccccc-cccc-cccc-cccc-cccccccccc19', '11111111-1111-1111-1111-111111111119', 5000000.00, NOW(), NOW())
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
    '11111111-1111-1111-1111-111111111119'
)
ORDER BY member_id;
