-- ============================================
-- 배포 기본 Seed Data - 상품 카테고리
-- ============================================
-- 기존 데이터는 유지하고, 동일한 의미의 카테고리가 없을 때만 추가합니다.
-- 루트 카테고리(depth=0)는 공용, 하위 카테고리(depth=1)는 판매자 기준으로 구성합니다.
-- ============================================

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
SELECT
    seed.category_id::UUID,
    NULL,
    NULL,
    seed.name,
    seed.description,
    0,
    seed.sort_order::INTEGER,
    NOW(),
    NOW()
FROM (
    VALUES
        ('94000000-0000-0000-0000-000000000001', '패션', '의류와 패션 굿즈를 모아보는 카테고리', 100),
        ('94000000-0000-0000-0000-000000000002', '문구/오피스', '기록과 꾸미기에 어울리는 문구 카테고리', 200),
        ('94000000-0000-0000-0000-000000000003', '리빙/테크', '생활 소품과 테크 액세서리 카테고리', 300)
) AS seed(category_id, name, description, sort_order)
WHERE NOT EXISTS (
    SELECT 1
    FROM product.category existing
    WHERE existing.parent_id IS NULL
      AND existing.seller_id IS NULL
      AND existing.depth = 0
      AND existing.deleted_at IS NULL
      AND existing.name = seed.name
);

WITH root_categories AS (
    SELECT DISTINCT ON (root.name)
        root.category_id,
        root.name
    FROM product.category root
    WHERE root.parent_id IS NULL
      AND root.seller_id IS NULL
      AND root.depth = 0
      AND root.deleted_at IS NULL
    ORDER BY root.name, root.sort_order, root.created_at
)
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
SELECT
    seed.category_id::UUID,
    root_categories.category_id,
    seller_member.member_id,
    seed.name,
    seed.description,
    1,
    seed.sort_order::INTEGER,
    NOW(),
    NOW()
FROM (
    VALUES
        (
            '94100000-0000-0000-0000-000000000001',
            '패션',
            'haneul.seller@seed.todaylunch.local',
            '티셔츠',
            '데일리웨어 중심의 반팔/긴팔 티셔츠',
            110
        ),
        (
            '94100000-0000-0000-0000-000000000002',
            '패션',
            'haneul.seller@seed.todaylunch.local',
            '후드/스웨트셔츠',
            '간절기와 겨울 시즌에 적합한 상의',
            120
        ),
        (
            '94100000-0000-0000-0000-000000000003',
            '문구/오피스',
            'haneul.seller@seed.todaylunch.local',
            '노트/다이어리',
            '학업과 기록용으로 활용하는 노트류',
            210
        ),
        (
            '94100000-0000-0000-0000-000000000004',
            '문구/오피스',
            'haneul.seller@seed.todaylunch.local',
            '스티커/데코',
            '다이어리 꾸미기와 포장용 소품',
            220
        ),
        (
            '94100000-0000-0000-0000-000000000005',
            '리빙/테크',
            'seoyun.seller@seed.todaylunch.local',
            '키링/뱃지',
            '가방과 파우치에 포인트를 더하는 소품',
            310
        ),
        (
            '94100000-0000-0000-0000-000000000006',
            '리빙/테크',
            'seoyun.seller@seed.todaylunch.local',
            '텀블러/머그',
            '사무실과 일상에서 쓰는 음용 굿즈',
            320
        )
) AS seed(category_id, parent_name, seller_email, name, description, sort_order)
JOIN root_categories
    ON root_categories.name = seed.parent_name
JOIN member.member seller_member
    ON seller_member.email = seed.seller_email
WHERE NOT EXISTS (
    SELECT 1
    FROM product.category existing
    WHERE existing.parent_id = root_categories.category_id
      AND existing.seller_id = seller_member.member_id
      AND existing.depth = 1
      AND existing.deleted_at IS NULL
      AND existing.name = seed.name
);


