-- ============================================
-- Cart Seed Data (개발용)
-- ============================================
-- member_id = '11111111-1111-1111-1111-111111111101' (김구매)
-- ============================================

INSERT INTO cart.cart (cart_id, member_id, product_id, quantity, added_at)
VALUES
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
     '11111111-1111-1111-1111-111111111101',
     'dddddddd-dddd-dddd-dddd-ddddddddd001',
     2, NOW())
ON CONFLICT (cart_id) DO NOTHING;
