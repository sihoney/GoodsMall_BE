-- ============================================
-- 배포 기본 Seed Data - AI 상품 임베딩
-- ============================================
-- prod_seed_product.sql로 적재된 기본 상품(9500... UUID)에 대한
-- 테스트/부트스트랩용 임베딩을 보장합니다.
-- 이미 존재하는 product_id 임베딩은 덮어쓰지 않습니다.
--
-- 벡터 구조 (1536차원):
--   [패션잡화 score, 문구 score, 의류 score, 생활용품 score, 디지털/전자 score, 0.001 × 1531]
-- ============================================

WITH target_products AS (
    SELECT
        product_row.product_id,
        parent_category.name AS parent_name
    FROM product.product product_row
    JOIN product.category child_category
        ON child_category.category_id = product_row.category_id
    JOIN product.category parent_category
        ON parent_category.category_id = child_category.parent_id
    WHERE product_row.deleted_at IS NULL
      AND product_row.product_id::text LIKE '95000000-%'
), scored AS (
    SELECT
        target_products.product_id,
        CASE WHEN parent_name = '패션잡화'    THEN 0.90 ELSE 0.05 END AS accessory_score,
        CASE WHEN parent_name = '문구'        THEN 0.90 ELSE 0.05 END AS stationery_score,
        CASE WHEN parent_name = '의류'        THEN 0.90 ELSE 0.05 END AS clothing_score,
        CASE WHEN parent_name = '생활용품'    THEN 0.90 ELSE 0.05 END AS living_score,
        CASE WHEN parent_name = '디지털/전자' THEN 0.90 ELSE 0.05 END AS digital_score
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
        || scored.accessory_score   || ','
        || scored.stationery_score  || ','
        || scored.clothing_score    || ','
        || scored.living_score      || ','
        || scored.digital_score     || ','
        || RTRIM(REPEAT('0.001,', 1531), ',')
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
)
ON CONFLICT (product_id) DO NOTHING;
