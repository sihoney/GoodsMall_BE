-- ============================================
-- Wallet Seed Data (개발용)
-- ============================================
-- member_id = '11111111-1111-1111-1111-111111111101' (김구매)
-- 초기 잔액: 500,000원
-- ============================================

INSERT INTO payment.wallet (wallet_id, member_id, balance, created_at, updated_at)
VALUES
    ('ffffffff-ffff-ffff-ffff-fffffffffff1',
     '11111111-1111-1111-1111-111111111101',
     500000, NOW(), NOW())
ON CONFLICT (wallet_id) DO NOTHING;
