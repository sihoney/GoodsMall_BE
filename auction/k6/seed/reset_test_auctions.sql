-- ============================================================
-- 부하테스트 경매 상태 초기화
-- 실행 시점: 각 테스트 시나리오 시작 직전
-- 실행 방법:
--   kubectl exec -i <postgres-pod> -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
--
-- 책임 범위:
--   auction 도메인: bid / outbox_event / current_highest_price 초기화
--   payment 도메인: auction_deposit / wallet_transaction 정리 + wallet 잔액 복원
--   → auction ↔ payment 상태를 항상 일치시켜 isFirst 판단 오류 방지
-- ============================================================

-- 1) payment.auction_deposit 관련 wallet_transaction 수집 후 정리
DO $$
DECLARE
    v_auction_ids uuid[] := ARRAY[
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105'::uuid,
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'::uuid
    ];
BEGIN
    -- wallet_transaction 삭제 (hold + refund 모두)
    DELETE FROM payment.wallet_transaction
    WHERE transaction_id IN (
        SELECT hold_wallet_transaction_id FROM payment.auction_deposit
        WHERE auction_id = ANY(v_auction_ids)
        UNION
        SELECT refund_wallet_transaction_id FROM payment.auction_deposit
        WHERE auction_id = ANY(v_auction_ids)
          AND refund_wallet_transaction_id IS NOT NULL
    );

    -- auction_deposit 삭제
    DELETE FROM payment.auction_deposit
    WHERE auction_id = ANY(v_auction_ids);
END $$;

-- 2) baseline 입찰자 wallet 잔액 복원 (5,000,000)
UPDATE payment.wallet
SET balance    = 5000000.00,
    updated_at = NOW()
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
);

-- 3) payment.outbox_events 정리
DELETE FROM payment.outbox_events
WHERE aggregate_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
)
OR aggregate_id IN (
    SELECT bid_id::text FROM auction.bid
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

-- 4) auction.outbox_event 정리
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

-- 5) 입찰 이력 정리
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

-- 6) current_highest_price 초기화 (부하테스트 경매 — 시간 그대로 유지)
UPDATE auction.auction
SET current_highest_price = NULL,
    ended_at              = scheduled_close_at,
    updated_at            = NOW()
WHERE auction_id IN (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

-- 7) smoke 전용 경매 상태 + 시간 리셋 (스케줄러가 만료시켜도 항상 ONGOING으로 복원)
UPDATE auction.auction
SET status                = 'ONGOING',
    current_highest_price = NULL,
    started_at            = NOW() - INTERVAL '1 hour',
    scheduled_close_at    = NOW() + INTERVAL '2 day',
    ended_at              = NOW() + INTERVAL '2 day',
    updated_at            = NOW()
WHERE auction_id = 'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001';
