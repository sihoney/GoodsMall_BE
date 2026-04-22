-- ============================================
-- Auction Seed Data (개발용)
-- ============================================
-- 전제 조건:
--   - product_id 'dddddddd-dddd-dddd-dddd-ddddddddd010' (한정판 콜라보 후드, type=AUCTION)
--   - seller_id  '22222222-2222-2222-2222-222222222202' (이판매)
--   - bidder_id  '11111111-1111-1111-1111-111111111101' (김구매)
-- 테스트 상태:
--   001 : ONGOING        - 시작 1시간 전, 1일 뒤 종료, 활성 입찰 2건 + 예치금 대기 PENDING 1건
--   002 : WAITING        - 2시간 후 시작 예정
--   003 : COMPLETED      - 과거에 종료, 낙찰+결제 완료
-- ============================================

INSERT INTO auction.auction (
    auction_id, product_id, seller_id,
    start_price, bid_unit, current_highest_price,
    started_at, scheduled_close_at, ended_at,
    status, created_at, updated_at
)
VALUES
    -- ONGOING
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'dddddddd-dddd-dddd-dddd-ddddddddd010',
        '22222222-2222-2222-2222-222222222202',
        50000.00, 1000.00, 52000.00,
        NOW() - INTERVAL '1 hour',
        NOW() + INTERVAL '1 day',
        NOW() + INTERVAL '1 day',
        'ONGOING',
        NOW() - INTERVAL '2 hour',
        NOW()
    ),
    -- WAITING
    (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee002',
        'dddddddd-dddd-dddd-dddd-ddddddddd010',
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
        '22222222-2222-2222-2222-222222222202',
        50000.00, 1000.00, 53000.00,
        NOW() - INTERVAL '3 day',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '1 day',
        'COMPLETED',
        NOW() - INTERVAL '3 day',
        NOW() - INTERVAL '1 day'
    )
ON CONFLICT (auction_id) DO NOTHING;

INSERT INTO auction.bid (
    bid_id, auction_id, bidder_id, bid_price, status, created_at, updated_at
)
VALUES
    -- ONGOING 경매 입찰 (최종 현재가 52000)
    (
        'ffffffff-ffff-ffff-ffff-fffffffff001',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        '11111111-1111-1111-1111-111111111101',
        51000.00, 'OUTBID',
        NOW() - INTERVAL '45 minute',
        NOW() - INTERVAL '30 minute'
    ),
    (
        'ffffffff-ffff-ffff-ffff-fffffffff002',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        '11111111-1111-1111-1111-111111111101',
        52000.00, 'ACTIVE',
        NOW() - INTERVAL '30 minute',
        NOW() - INTERVAL '30 minute'
    ),
    -- ONGOING 경매 예치금 처리 대기 입찰 (PENDING, confirm 시 ACTIVE 전이 예정)
    (
        'ffffffff-ffff-ffff-ffff-fffffffff006',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        '11111111-1111-1111-1111-111111111101',
        53000.00, 'PENDING',
        NOW() - INTERVAL '1 minute',
        NOW() - INTERVAL '1 minute'
    ),
    -- COMPLETED 경매 입찰 (최종 낙찰가 53000, 결제 완료)
    (
        'ffffffff-ffff-ffff-ffff-fffffffff003',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003',
        '11111111-1111-1111-1111-111111111101',
        51000.00, 'OUTBID',
        NOW() - INTERVAL '2 day 3 hour',
        NOW() - INTERVAL '2 day 2 hour'
    ),
    (
        'ffffffff-ffff-ffff-ffff-fffffffff004',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003',
        '11111111-1111-1111-1111-111111111101',
        52000.00, 'OUTBID',
        NOW() - INTERVAL '2 day 2 hour',
        NOW() - INTERVAL '2 day 1 hour'
    ),
    (
        'ffffffff-ffff-ffff-ffff-fffffffff005',
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003',
        '11111111-1111-1111-1111-111111111101',
        53000.00, 'PAYMENT_COMPLETED',
        NOW() - INTERVAL '2 day 1 hour',
        NOW() - INTERVAL '1 day'
    )
ON CONFLICT (bid_id) DO NOTHING;
