-- ============================================
-- Wallet Seed Data (개발용)
-- ============================================
-- 김구매·이판매·관리자 지갑은 dev_seed_payment_settlement.sql 에서 관리
-- (charge/wallet_transaction FK 참조 wallet_id와 일치시켜야 함)
-- 여기서는 경매 다중 입찰 테스트용 신규 구매자만 추가
-- ============================================

INSERT INTO payment.wallet (wallet_id, member_id, balance, created_at, updated_at)
VALUES
    ('ffffffff-ffff-ffff-ffff-fffffffffff2',
     '11111111-1111-1111-1111-111111111102',
     300000.00, NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-fffffffffff3',
     '11111111-1111-1111-1111-111111111103',
     300000.00, NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;
