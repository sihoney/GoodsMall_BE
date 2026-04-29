-- ============================================================
-- 부하테스트 경매 상태 초기화
-- 실행 시점: 각 테스트 시나리오 시작 직전
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -- psql -U <user> -d <db> \
--     -c "\i /path/to/reset_test_auctions.sql"
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

-- 2) 입찰 이력 정리 (선택 — 테이블 이름은 실제 스키마에 맞게 확인 후 실행)
-- DELETE FROM auction.bid
-- WHERE auction_id IN (
--     'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
--     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
-- );
