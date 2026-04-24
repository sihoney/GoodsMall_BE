-- ============================================
-- 배포 기본 Seed Data - AI 상품 임베딩
-- ============================================
-- prod_seed_product.sql로 적재된 기본 상품(9500... UUID)에 대한
-- 테스트/부트스트랩용 임베딩을 보장합니다.
-- 이미 존재하는 product_id 임베딩은 덮어쓰지 않습니다.
-- ============================================

WITH target_products AS (
    SELECT
        product_row.product_id,
        category_row.name AS category_name
    FROM product.product product_row
    JOIN product.category category_row
      ON category_row.category_id = product_row.category_id
    WHERE product_row.deleted_at IS NULL
      AND product_row.product_id::text LIKE '95000000-%'
), scored AS (
    SELECT
        target_products.product_id,
        CASE
            WHEN target_products.category_name IN ('티셔츠', '후드/스웨트셔츠') THEN 0.90
            WHEN target_products.category_name IN ('노트/다이어리', '스티커/데코') THEN 0.08
            WHEN target_products.category_name IN ('키링/뱃지', '텀블러/머그') THEN 0.12
            ELSE 0.33
        END AS fashion_score,
        CASE
            WHEN target_products.category_name IN ('티셔츠', '후드/스웨트셔츠') THEN 0.08
            WHEN target_products.category_name IN ('노트/다이어리', '스티커/데코') THEN 0.90
            WHEN target_products.category_name IN ('키링/뱃지', '텀블러/머그') THEN 0.10
            ELSE 0.33
        END AS stationery_score,
        CASE
            WHEN target_products.category_name IN ('티셔츠', '후드/스웨트셔츠') THEN 0.12
            WHEN target_products.category_name IN ('노트/다이어리', '스티커/데코') THEN 0.10
            WHEN target_products.category_name IN ('키링/뱃지', '텀블러/머그') THEN 0.90
            ELSE 0.34
        END AS living_score
    FROM target_products
)
INSERT INTO ai.product_embedding (
    embedding_id,
    product_id,
    embedding,
    source_updated_at,
    is_active,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTRING(MD5(scored.product_id::text || ':embedding'), 1, 8)
        || '-'
        || SUBSTRING(MD5(scored.product_id::text || ':embedding'), 9, 4)
        || '-'
        || SUBSTRING(MD5(scored.product_id::text || ':embedding'), 13, 4)
        || '-'
        || SUBSTRING(MD5(scored.product_id::text || ':embedding'), 17, 4)
        || '-'
        || SUBSTRING(MD5(scored.product_id::text || ':embedding'), 21, 12)
    )::UUID,
    scored.product_id,
    CAST(
        '['
        || scored.fashion_score || ','
        || scored.stationery_score || ','
        || scored.living_score || ','
        || RTRIM(REPEAT('0.001,', 1533), ',')
        || ']'
        AS vector
    ),
    NOW(),
    true,
    NOW(),
    NOW()
FROM scored
WHERE NOT EXISTS (
    SELECT 1
    FROM ai.product_embedding existing
    WHERE existing.product_id = scored.product_id
);

