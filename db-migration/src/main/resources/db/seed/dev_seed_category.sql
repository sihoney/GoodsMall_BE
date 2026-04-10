-- ============================================
-- Category Seed Data (개발용) - 굿즈 샵
-- ============================================
-- 3-tier 계층 구조: 대분류(depth=0) → 중분류(depth=1) → 소분류(depth=2)
-- 대분류: 관리자 관리 (seller_id=NULL)
-- 중/소분류: 판매자 관리 (seller_id = 이판매)
-- ============================================

-- 대분류 (depth=0)
INSERT INTO product.category (category_id, parent_id, seller_id, name, description, depth, sort_order, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001', NULL, NULL, '의류', '의류 및 패션 굿즈', 0, 1, NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002', NULL, NULL, '문구', '문구 및 사무용품 굿즈', 0, 2, NOW(), NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003', NULL, NULL, '액세서리', '액세서리 및 소품 굿즈', 0, 3, NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;

INSERT INTO product.category (
    category_id,
    parent_id,
    seller_id,
    name,
    description,
    depth,
    sort_order,
    created_at,
    updated_at
)
VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccc001', NULL, NULL, 'T-shirt', 'Short-sleeve apparel category', 0, 11, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc002', NULL, NULL, 'Hoodie', 'Hoodie apparel category', 0, 12, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc003', NULL, NULL, 'Cap', 'Hat and cap category', 0, 13, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc004', NULL, NULL, 'Notebook', 'Notebook and memo category', 0, 14, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc005', NULL, NULL, 'Sticker', 'Sticker and deco item category', 0, 15, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc006', NULL, NULL, 'Keyring', 'Keyring accessory category', 0, 16, NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccc007', NULL, NULL, 'Phone Case', 'Phone case accessory category', 0, 17, NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;

