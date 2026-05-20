-- ============================================================
-- smoke / baseline 테스트 전용 경매 시드
-- 실행 시점: 최초 1회 (경매가 DB에 없을 때)
-- 실행 방법:
--   kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- \
--     psql -U goods -d goods_mall < ~/k6/seed/seed_test_auctions.sql
--
-- 포함 경매:
--   eeeeeeee-...-001 : ONGOING  (smoke / baseline 공용)
--   eeeeeeee-...-002 : WAITING
--   eeeeeeee-...-003 : COMPLETED
-- 부하테스트 경매(aaaaaaaa-...)는 load_test_auctions.sql 에서 별도 관리
-- ============================================================

INSERT INTO auction.auction (
    auction_id, product_id, product_title, thumbnail_key, seller_id,
    start_price, bid_unit, current_highest_price,
    started_at, scheduled_close_at, ended_at,
    status, created_at, updated_at
)
VALUES
    -- ONGOING: smoke / baseline 테스트 대상
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'dddddddd-dddd-dddd-dddd-ddddddddd010',
        '한정판 콜라보 후드 (경매)',
        'test-thumbnail-smoke.jpg',
        '22222222-2222-2222-2222-222222222202',
        50000.00, 1000.00, NULL,
        NOW() - INTERVAL '1 hour',
        NOW() + INTERVAL '2 day',
        NOW() + INTERVAL '2 day',
        'ONGOING',
        NOW() - INTERVAL '1 hour',
        NOW()
    ),
    -- WAITING
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee002',
        'dddddddd-dddd-dddd-dddd-ddddddddd010',
        '한정판 콜라보 후드 (경매)',
        'test-thumbnail-smoke.jpg',
        '22222222-2222-2222-2222-222222222202',
        50000.00, 1000.00, NULL,
        NOW() + INTERVAL '2 hour',
        NOW() + INTERVAL '2 day',
        NOW() + INTERVAL '2 day',
        'WAITING',
        NOW(),
        NOW()
    ),
    -- COMPLETED
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003',
        'dddddddd-dddd-dddd-dddd-ddddddddd010',
        '한정판 콜라보 후드 (경매)',
        'test-thumbnail-smoke.jpg',
        '22222222-2222-2222-2222-222222222202',
        50000.00, 1000.00, 53000.00,
        NOW() - INTERVAL '3 day',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day',
        'COMPLETED',
        NOW() - INTERVAL '3 day',
        NOW() - INTERVAL '1 day'
    )
ON CONFLICT (auction_id) DO UPDATE
    SET product_title      = EXCLUDED.product_title,
        thumbnail_key      = EXCLUDED.thumbnail_key,
        status             = EXCLUDED.status,
        started_at         = EXCLUDED.started_at,
        scheduled_close_at = EXCLUDED.scheduled_close_at,
        ended_at           = EXCLUDED.ended_at,
        updated_at         = NOW();

-- 확인
SELECT auction_id, product_title, thumbnail_key, status
FROM auction.auction
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee002',
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003'
)
ORDER BY auction_id;
