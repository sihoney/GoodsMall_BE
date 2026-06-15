-- ============================================
-- 운영 시드 데이터 - 상품
-- ============================================
-- docs_private/seed-images/seed_product_manifest.csv 기준 생성
-- 직접 수정 대신 manifest 수정 후 scripts/generate-product-seed-from-images.ps1 재실행 권장

INSERT INTO product.product (
    product_id,
    seller_id,
    category_id,
    title,
    description,
    price,
    stock_quantity,
    status,
    type,
    view_count,
    created_at,
    updated_at
)
SELECT
    seed.product_id::UUID,
    seller_member.member_id,
    seed.category_id::UUID,
    seed.title,
    seed.description,
    seed.price::DECIMAL(10, 2),
    seed.stock_quantity::INTEGER,
    seed.status,
    'GENERAL',
    0,
    NOW(),
    NOW()
FROM (
    VALUES
        ('95000000-0000-0000-0000-000000000001', 'seller1@todaylunchmenu.com', '94100000-0000-0000-0000-000000000002', '데일리 귀걸이 세트', '일상 속 포인트 아이템으로 착용하기 좋은 굿즈 귀걸이 세트입니다.', 12900, 80, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000002', 'seller1@todaylunchmenu.com', '94100000-0000-0000-0000-000000000003', '베이직 굿즈 가방', '가볍게 들고 다니기 좋은 캐주얼 데일리 굿즈 가방입니다.', 14900, 100, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000003', 'seller2@todaylunchmenu.com', '94100000-0000-0000-0000-000000000003', '빅 굿즈 가방', '넉넉한 수납공간을 갖춘 실용적인 대형 굿즈 가방입니다.', 22900, 60, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000004', 'seller2@todaylunchmenu.com', '94100000-0000-0000-0000-000000000002', '굿즈 팔찌', '간단한 포인트 액세서리로 활용하기 좋은 캐주얼 팔찌입니다.', 8900, 120, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000005', 'seller3@todaylunchmenu.com', '94100000-0000-0000-0000-000000000016', '데스크 소품', '작업 공간을 정리하고 분위기를 더해주는 데스크 소품입니다.', 11900, 90, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000006', 'seller3@todaylunchmenu.com', '94100000-0000-0000-0000-000000000010', '굿즈 캡', '일상에서 편하게 착용할 수 있는 베이직 굿즈 캡입니다.', 18900, 70, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000007', 'seller4@todaylunchmenu.com', '94100000-0000-0000-0000-000000000002', '굿즈 목걸이', '깔끔한 포인트를 더해주는 심플한 굿즈 목걸이입니다.', 9900, 85, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000008', 'seller4@todaylunchmenu.com', '94100000-0000-0000-0000-000000000002', '굿즈 반지', '매일 부담 없이 착용하기 좋은 미니멀한 굿즈 반지입니다.', 7900, 110, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000009', 'seller5@todaylunchmenu.com', '94100000-0000-0000-0000-000000000008', '굿즈 상의', '심플한 굿즈 디자인을 담은 깔끔한 데일리 상의입니다.', 25900, 50, 'ACTIVE'),
        ('95000000-0000-0000-0000-000000000010', 'seller5@todaylunchmenu.com', '94100000-0000-0000-0000-000000000008', '베이직 굿즈 티셔츠', '편안한 데일리 착용을 위해 제작된 베이직 굿즈 티셔츠입니다.', 19900, 95, 'ACTIVE')
) AS seed(product_id, seller_email, category_id, title, description, price, stock_quantity, status)
JOIN member.member seller_member
    ON seller_member.email = seed.seller_email
ON CONFLICT (product_id) DO UPDATE SET
    seller_id = EXCLUDED.seller_id,
    category_id = EXCLUDED.category_id,
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    stock_quantity = EXCLUDED.stock_quantity,
    status = EXCLUDED.status,
    type = EXCLUDED.type,
    updated_at = NOW();
