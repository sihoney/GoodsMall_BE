-- ============================================================
-- 부하테스트 경매 상태 초기화 (light reset)
-- 실행 시점: 각 테스트 시나리오 시작 직전
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -- psql -U <user> -d <db> \
--     -c "\i /path/to/reset_test_auctions.sql"
--
-- 책임 범위: auction 도메인만 정리 (bid 누적 / outbox 누적 / 최고가 초기화).
-- payment 측 데이터(auction_deposit, wallet_transaction, wallet 잔액)는
--   - 각 시나리오 사이에는 그대로 둠 (잔액 누적 문제는 refill_wallet.sql 사용)
--   - 테스트 완전 종료 시점에는 cleanup_test_data.sql 사용
-- ============================================================

-- 1) current_highest_price 초기화
UPDATE auction.auction
SET current_highest_price = NULL,
    updated_at             = NOW()
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',  -- ONGOING 시드 경매
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',   -- 부하테스트 경매 1
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',   -- 부하테스트 경매 2
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',   -- 부하테스트 경매 3
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',   -- 부하테스트 경매 4
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',   -- 부하테스트 경매 5
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'    -- 락테스트 경매
);

-- 2) auction.outbox_event 정리
--    aggregate_id 는 auction_id 또는 bid_id 둘 다 사용되므로 두 케이스 모두 처리
DELETE FROM auction.outbox_event
WHERE aggregate_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'::uuid
)
OR aggregate_id IN (
    SELECT bid_id FROM auction.bid
    WHERE auction_id IN (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
    )
);

-- 3) 입찰 이력 정리
DELETE FROM auction.bid
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);
