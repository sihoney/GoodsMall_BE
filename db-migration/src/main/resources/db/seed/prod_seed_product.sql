-- ============================================
-- 배포 기본 Seed Data - 상품
-- ============================================
-- 이미지 없이 실제 운영 데이터와 비슷한 형태의 기본 상품만 추가합니다.
-- 기존 상품은 유지하고, 같은 판매자의 동일 제목 상품이 없을 때만 추가합니다.
-- ============================================

WITH seller_categories AS (
    SELECT DISTINCT ON (parent_category.name, child_category.name, child_category.seller_id)
        parent_category.name AS parent_name,
        child_category.name AS category_name,
        child_category.seller_id,
        child_category.category_id
    FROM product.category child_category
    JOIN product.category parent_category
        ON parent_category.category_id = child_category.parent_id
    WHERE child_category.depth = 1
      AND child_category.deleted_at IS NULL
      AND parent_category.deleted_at IS NULL
    ORDER BY parent_category.name, child_category.name, child_category.seller_id, child_category.sort_order, child_category.created_at
)
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
    seller_categories.category_id,
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
        (
            '95000000-0000-0000-0000-000000000001',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '티셔츠',
            '투데이런치 로고 반팔 굿즈 티셔츠',
            '투데이런치 시그니처 로고를 전면에 배치한 베이직 굿즈 티셔츠',
            29000.00,
            80,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000002',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '후드/스웨트셔츠',
            '투데이런치 응원 후드 굿즈',
            '행사 응원용으로 제작한 기모 안감의 오버핏 후드 굿즈',
            52000.00,
            45,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000003',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '노트/다이어리',
            '팬덤 스터디 플래너 굿즈 노트',
            '일정과 목표를 기록하는 팬덤 테마의 주간 플래너 굿즈 노트',
            7800.00,
            150,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000004',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '스티커/데코',
            '캐릭터 표정 굿즈 스티커팩',
            '공식 캐릭터 표정과 말풍선을 담은 다이어리 데코 굿즈 스티커 24매',
            3900.00,
            220,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000005',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '투데이런치 아크릴 굿즈 키링',
            '팀 로고와 마스코트 일러스트를 담은 투명 아크릴 굿즈 키링',
            6900.00,
            140,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000006',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '공식 로고 보온 굿즈 텀블러',
            '행사 참여자용으로 제작한 473ml 보온 스테인리스 굿즈 텀블러',
            18900.00,
            60,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000007',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '데스크 굿즈 머그컵 세트',
            '로고 머그컵과 슬로건 코스터를 함께 구성한 데스크 굿즈 세트',
            14900.00,
            0,
            'SOLD_OUT'
        ),
        (
            '95000000-0000-0000-0000-000000000008',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '티셔츠',
            '레트로 콘서트 굿즈 반팔 티셔츠',
            '투어 포스터 감성을 살린 레트로 프린트 굿즈 반팔 티셔츠',
            31000.00,
            90,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000009',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '티셔츠',
            '팬클럽 스트라이프 굿즈 롱슬리브',
            '팬클럽 컬러 스트라이프를 적용한 시즌 한정 굿즈 롱슬리브',
            34000.00,
            70,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000010',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '티셔츠',
            '아치 로고 피그먼트 굿즈 티셔츠',
            '빈티지 워싱 공정을 더한 아치 로고 스타일의 굿즈 티셔츠',
            36000.00,
            55,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000011',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '후드/스웨트셔츠',
            '미니멀 자수 로고 굿즈 스웨트셔츠',
            '공식 로고 자수를 넣은 데일리 착용용 굿즈 스웨트셔츠',
            43000.00,
            65,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000012',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '후드/스웨트셔츠',
            '응원 컬러블록 굿즈 후드 집업',
            '응원전 테마 컬러를 적용한 배색 디자인의 굿즈 후드 집업',
            59000.00,
            38,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000013',
            'haneul.seller@seed.todaylunch.local',
            '패션',
            '후드/스웨트셔츠',
            '헤비웨이트 시그니처 굿즈 후드',
            '겨울 시즌 팬미팅용으로 제작한 보온성 높은 헤비웨이트 굿즈 후드',
            64000.00,
            32,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000014',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '노트/다이어리',
            '무드 트래커 굿즈 저널',
            '캐릭터 스탬프 템플릿이 포함된 팬 기록용 만년형 굿즈 저널',
            9800.00,
            130,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000015',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '노트/다이어리',
            'A5 굿즈 도트 노트 리필팩',
            '팬아트 스케치와 필기를 함께 할 수 있는 굿즈 도트지 리필팩',
            6200.00,
            210,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000016',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '노트/다이어리',
            '주간 목표 굿즈 플래너 패드',
            '팬 활동 일정 관리에 맞춘 떼어쓰는 패드형 굿즈 플래너',
            5500.00,
            240,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000017',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '스티커/데코',
            '시즌 콘셉트 굿즈 스티커 북',
            '봄·여름·가을·겨울 콘셉트 일러스트를 담은 공식 굿즈 스티커 북',
            7900.00,
            180,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000018',
            'haneul.seller@seed.todaylunch.local',
            '문구/오피스',
            '스티커/데코',
            '투명 로고 굿즈 라벨 스티커 세트',
            '응원봉 케이스와 문구류를 꾸밀 수 있는 방수 굿즈 라벨 스티커',
            4800.00,
            260,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000019',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '마스코트 금속 참 굿즈 키링',
            '마스코트 얼굴 참 장식을 더한 한정판 금속 굿즈 키링',
            8200.00,
            0,
            'SOLD_OUT'
        ),
        (
            '95000000-0000-0000-0000-000000000020',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '플라워 엠블럼 굿즈 키링',
            '공식 엠블럼을 꽃 모티프로 재해석한 레진 굿즈 키링',
            8900.00,
            110,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000021',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '메탈 로고 굿즈 뱃지',
            '재킷과 가방에 부착 가능한 고광택 메탈 로고 굿즈 뱃지',
            5900.00,
            160,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000022',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '야광 로고 굿즈 키링',
            '공연장 야간 이동 시 포인트가 되는 야광 로고 굿즈 키링',
            7200.00,
            95,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000023',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '에폭시 캐릭터 굿즈 키링',
            '스크래치 방지 코팅을 적용한 공식 캐릭터 에폭시 굿즈 키링',
            7600.00,
            120,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000024',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '포토 프린트 굿즈 내열 머그컵',
            '아티스트 포토 프린트를 더한 400ml 내열 유리 굿즈 머그컵',
            13200.00,
            85,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000025',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '시그니처 이중 진공 굿즈 텀블러 600ml',
            '장시간 보온과 보냉이 가능한 시그니처 컬러의 굿즈 텀블러',
            22900.00,
            50,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000026',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '응원전 스트로 굿즈 텀블러 710ml',
            '긴 행사에서도 사용하기 좋은 스트로 포함 대용량 굿즈 텀블러',
            19900.00,
            72,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000027',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '아웃도어 굿즈 스태킹 머그',
            '원정 응원과 캠핑 테마에 맞춘 스태킹 구조의 굿즈 머그컵',
            11900.00,
            66,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000028',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '텀블러/머그',
            '데일리 슬로건 굿즈 세라믹 머그',
            '슬로건 문구를 각인한 350ml 데일리 굿즈 세라믹 머그',
            9900.00,
            140,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000029',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '아크릴 포토카드 굿즈 키링',
            '포토카드 콘셉트를 적용한 투명 프레임형 아크릴 굿즈 키링',
            8300.00,
            88,
            'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000030',
            'seoyun.seller@seed.todaylunch.local',
            '리빙/테크',
            '키링/뱃지',
            '미니 참 굿즈 키링 세트',
            '유닛별 상징 참 장식을 조합한 컬렉션형 굿즈 키링 세트',
            9700.00,
            0,
            'SOLD_OUT'
        )
) AS seed(product_id, seller_email, parent_name, category_name, title, description, price, stock_quantity, status)
JOIN member.member seller_member
    ON seller_member.email = seed.seller_email
JOIN seller_categories
    ON seller_categories.parent_name = seed.parent_name
   AND seller_categories.category_name = seed.category_name
   AND seller_categories.seller_id = seller_member.member_id
WHERE NOT EXISTS (
    SELECT 1
    FROM product.product existing
    WHERE existing.seller_id = seller_member.member_id
      AND existing.deleted_at IS NULL
      AND existing.title = seed.title
);


