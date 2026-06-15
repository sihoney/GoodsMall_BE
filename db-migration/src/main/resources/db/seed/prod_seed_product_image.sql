-- ============================================
-- 운영 시드 데이터 - 상품 이미지
-- ============================================
-- docs_private/seed-images/seed_product_manifest.csv 기준 생성
-- 이미지 표시 전 docs_private/seed-upload 파일의 S3 업로드 필요

WITH seed_images(product_id, image_id, s3_key, sort_order, is_thumbnail) AS (
    VALUES
        -- 귀걸이 세트
        ('95000000-0000-0000-0000-000000000001', '95200000-0000-0000-0000-000000000001', 'products/seed/earring-set/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000001', '95300000-0000-0000-0000-000000000001', 'products/seed/earring-set/detail-1.jpg', 1, false),
        -- 기본 굿즈 가방
        ('95000000-0000-0000-0000-000000000002', '95200000-0000-0000-0000-000000000002', 'products/seed/basic-bag/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000002', '95300000-0000-0000-0000-000000000002', 'products/seed/basic-bag/detail-1.jpg', 1, false),
        -- 대형 굿즈 가방
        ('95000000-0000-0000-0000-000000000003', '95200000-0000-0000-0000-000000000003', 'products/seed/big-bag/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000003', '95300000-0000-0000-0000-000000000003', 'products/seed/big-bag/detail-1.jpg', 1, false),
        -- 굿즈 팔찌
        ('95000000-0000-0000-0000-000000000004', '95200000-0000-0000-0000-000000000004', 'products/seed/bracelet/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000004', '95300000-0000-0000-0000-000000000004', 'products/seed/bracelet/detail-1.jpg', 1, false),
        -- 책상 소품
        ('95000000-0000-0000-0000-000000000005', '95200000-0000-0000-0000-000000000005', 'products/seed/desk-accessory/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000005', '95300000-0000-0000-0000-000000000005', 'products/seed/desk-accessory/detail-1.jpg', 1, false),
        -- 굿즈 모자
        ('95000000-0000-0000-0000-000000000006', '95200000-0000-0000-0000-000000000006', 'products/seed/hat/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000006', '95300000-0000-0000-0000-000000000006', 'products/seed/hat/detail-1.jpg', 1, false),
        -- 굿즈 목걸이
        ('95000000-0000-0000-0000-000000000007', '95200000-0000-0000-0000-000000000007', 'products/seed/necklace/thumbnail.jpg', 0, true),
        -- 굿즈 반지
        ('95000000-0000-0000-0000-000000000008', '95200000-0000-0000-0000-000000000008', 'products/seed/ring/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000008', '95300000-0000-0000-0000-000000000008', 'products/seed/ring/detail-1.jpg', 1, false),
        -- 굿즈 상의
        ('95000000-0000-0000-0000-000000000009', '95200000-0000-0000-0000-000000000009', 'products/seed/top/thumbnail.jpg', 0, true),
        -- 굿즈 티셔츠
        ('95000000-0000-0000-0000-000000000010', '95200000-0000-0000-0000-000000000010', 'products/seed/tshirt/thumbnail.jpg', 0, true),
        ('95000000-0000-0000-0000-000000000010', '95300000-0000-0000-0000-000000000010', 'products/seed/tshirt/detail-1.jpg', 1, false)
)
INSERT INTO product.product_image (
    image_id,
    product_id,
    s3_key,
    sort_order,
    is_thumbnail,
    created_at
)
SELECT
    seed.image_id::UUID,
    seed.product_id::UUID,
    seed.s3_key,
    seed.sort_order,
    seed.is_thumbnail,
    NOW()
FROM seed_images seed
JOIN product.product product_row
    ON product_row.product_id = seed.product_id::UUID
ON CONFLICT (image_id) DO UPDATE SET
    s3_key = EXCLUDED.s3_key,
    sort_order = EXCLUDED.sort_order,
    is_thumbnail = EXCLUDED.is_thumbnail;
