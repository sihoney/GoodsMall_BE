-- ============================================
-- 부하테스트 전용 경매 데이터
-- 실행 시점: 부하테스트 시작 직전 (서비스 기동 후)
-- 전제 조건:
--   - seller_id '22222222-2222-2222-2222-222222222202' (이판매) 존재
--   - product_id 'dddddddd-dddd-dddd-dddd-ddddddddd010' 존재
-- ============================================

-- 부하테스트용 경매 풀 (읽기/쓰기 혼합 시나리오 B, C, D, E)
INSERT INTO auction.auction (
    auction_id, product_id, product_title, thumbnail_key, seller_id,
    start_price, bid_unit, current_highest_price,
    started_at, scheduled_close_at, ended_at,
    status, created_at, updated_at
)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '부하테스트 경매 1', 'test-thumbnail-1.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 day', NOW() + INTERVAL '2 day', 'ONGOING', NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '부하테스트 경매 2', 'test-thumbnail-2.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 day', NOW() + INTERVAL '2 day', 'ONGOING', NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '부하테스트 경매 3', 'test-thumbnail-3.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 day', NOW() + INTERVAL '2 day', 'ONGOING', NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '부하테스트 경매 4', 'test-thumbnail-4.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 day', NOW() + INTERVAL '2 day', 'ONGOING', NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '부하테스트 경매 5', 'test-thumbnail-5.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '2 day', NOW() + INTERVAL '2 day', 'ONGOING', NOW(), NOW()),

    -- 동시 입찰 전용 경매 (시나리오 A) — 락 경합 테스트
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201', 'dddddddd-dddd-dddd-dddd-ddddddddd010', '[락테스트] 동시 입찰 경매', 'test-thumbnail-lock.jpg', '22222222-2222-2222-2222-222222222202', 50000, 1000, NULL, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '3 day', NOW() + INTERVAL '3 day', 'ONGOING', NOW(), NOW())

ON CONFLICT (auction_id) DO NOTHING;
