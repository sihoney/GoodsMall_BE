-- ============================================
-- Wallet Seed Data (개발용)
-- ============================================
-- 비밀번호: buyer(1111), admin(3333)
-- 잔액 기준: 시초가 50,000 + 수수료 10% 고려
--   김구매  500,000 / 박입찰  300,000 / 최경매  300,000 / 관리자  100,000
-- ============================================

INSERT INTO payment.wallet (wallet_id, member_id, balance, created_at, updated_at)
VALUES
    ('ffffffff-ffff-ffff-ffff-fffffffffff1',
     '11111111-1111-1111-1111-111111111101',
     500000.00, NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-fffffffffff2',
     '11111111-1111-1111-1111-111111111102',
     300000.00, NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-fffffffffff3',
     '11111111-1111-1111-1111-111111111103',
     300000.00, NOW(), NOW()),
    ('ffffffff-ffff-ffff-ffff-fffffffffff4',
     '33333333-3333-3333-3333-333333333303',
     100000.00, NOW(), NOW())
ON CONFLICT (wallet_id) DO NOTHING;
