-- ============================================================
-- 부하테스트 wallet 잔액 충전
-- 실행 시점: soak / 장기 시나리오 시작 전, 잔액이 부족할 때
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -- psql -U <user> -d <db> \
--     -f /path/to/refill_wallet.sql
--
-- 주의: reset_test_auctions.sql 은 auction 도메인만 정리하므로
--       payment.wallet 잔액은 시나리오 사이에 그대로 누적 차감된다.
--       이 스크립트는 그 잔액을 시드 초기값(1,000,000)으로 강제 복원한다.
--       payment.wallet_transaction / payment.auction_deposit 은 건드리지 않으므로
--       완전한 정합성 회복은 cleanup_test_data.sql 사용.
-- ============================================================

UPDATE payment.wallet
SET balance    = 1000000.00,
    updated_at = NOW()
WHERE member_id IN (
    '11111111-1111-1111-1111-111111111101',  -- buyer
    '11111111-1111-1111-1111-111111111102',  -- buyer2
    '11111111-1111-1111-1111-111111111103',  -- buyer3
    '22222222-2222-2222-2222-222222222202'   -- seller
);

-- 충전 결과 확인
SELECT member_id, balance
FROM payment.wallet
WHERE member_id IN (
    '11111111-1111-1111-1111-111111111101',
    '11111111-1111-1111-1111-111111111102',
    '11111111-1111-1111-1111-111111111103',
    '22222222-2222-2222-2222-222222222202'
);
