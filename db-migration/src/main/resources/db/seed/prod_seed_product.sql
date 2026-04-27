-- ============================================
-- 배포 기본 Seed Data - 상품 (굿즈몰)
-- ============================================
-- seller1(송하늘): 패션잡화 > 키링·핀뱃지, 문구 > 스티커         (6개)
-- seller2(최서윤): 패션잡화 > 가방, 문구 > 엽서/카드·노트/다이어리 (6개)
-- seller3(박굿즈): 문구 > 인쇄물, 의류 > 상의·아우터              (7개)
-- seller4(이한정): 의류 > 기타, 생활용품 > 주방·인테리어           (7개)
-- seller5(정마켓): 생활용품 > 기타 생활, 디지털/전자 전체          (7개)
-- 기존 동일 판매자·동일 제목 상품이 없을 때만 추가합니다.
-- ============================================

WITH target_categories AS (
    SELECT
        child.category_id,
        parent.name AS parent_name,
        child.name  AS category_name
    FROM product.category child
    JOIN product.category parent ON parent.category_id = child.parent_id
    WHERE child.depth = 1
      AND child.seller_id IS NULL
      AND child.deleted_at IS NULL
      AND parent.deleted_at IS NULL
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
    target_categories.category_id,
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
        -- seller1 (송하늘) : 패션잡화 > 키링
        (
            '95000000-0000-0000-0000-000000000001',
            'seller1@todaylunchmenu.com',
            '패션잡화', '키링',
            '투데이런치 아크릴 굿즈 키링',
            '팀 로고와 마스코트 일러스트를 담은 투명 아크릴 굿즈 키링',
            6900.00, 140, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000002',
            'seller1@todaylunchmenu.com',
            '패션잡화', '키링',
            '마스코트 레더 굿즈 키링',
            '부드러운 레더 소재에 마스코트를 새긴 프리미엄 굿즈 키링',
            8200.00, 80, 'ACTIVE'
        ),
        -- seller1 (송하늘) : 패션잡화 > 핀뱃지
        (
            '95000000-0000-0000-0000-000000000003',
            'seller1@todaylunchmenu.com',
            '패션잡화', '핀뱃지',
            '시그니처 금속 굿즈 핀뱃지',
            '고광택 메탈 소재의 공식 로고 굿즈 핀뱃지',
            5900.00, 160, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000004',
            'seller1@todaylunchmenu.com',
            '패션잡화', '핀뱃지',
            '캐릭터 자수 굿즈 뱃지',
            '공식 캐릭터를 자수로 구현한 패브릭 굿즈 핀뱃지',
            6500.00, 120, 'ACTIVE'
        ),
        -- seller1 (송하늘) : 문구 > 스티커
        (
            '95000000-0000-0000-0000-000000000005',
            'seller1@todaylunchmenu.com',
            '문구', '스티커',
            '캐릭터 표정 굿즈 스티커팩',
            '공식 캐릭터 표정과 말풍선을 담은 다이어리 데코 굿즈 스티커 24매',
            3900.00, 220, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000006',
            'seller1@todaylunchmenu.com',
            '문구', '스티커',
            '시즌 콘셉트 굿즈 홀로그램 스티커북',
            '봄·여름·가을·겨울 콘셉트 일러스트를 담은 공식 굿즈 홀로그램 스티커북',
            7900.00, 180, 'ACTIVE'
        ),

        -- seller2 (최서윤) : 패션잡화 > 가방
        (
            '95000000-0000-0000-0000-000000000007',
            'seller2@todaylunchmenu.com',
            '패션잡화', '가방',
            '로고 캔버스 굿즈 에코백',
            '시그니처 로고를 프린팅한 데일리 캔버스 굿즈 에코백',
            14900.00, 90, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000008',
            'seller2@todaylunchmenu.com',
            '패션잡화', '가방',
            '마스코트 굿즈 파우치백',
            '마스코트 일러스트를 전면 프린팅한 소품 수납용 굿즈 파우치백',
            18000.00, 60, 'ACTIVE'
        ),
        -- seller2 (최서윤) : 문구 > 엽서/카드
        (
            '95000000-0000-0000-0000-000000000009',
            'seller2@todaylunchmenu.com',
            '문구', '엽서/카드',
            '일러스트 굿즈 엽서 5종 세트',
            '따뜻한 색감의 공식 일러스트 굿즈 엽서 5종 세트',
            5900.00, 130, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000010',
            'seller2@todaylunchmenu.com',
            '문구', '엽서/카드',
            '팬덤 굿즈 포토카드 6종 세트',
            '팬클럽 전용 포토 콘셉트의 굿즈 포토카드 6종 세트',
            4500.00, 200, 'ACTIVE'
        ),
        -- seller2 (최서윤) : 문구 > 노트/다이어리
        (
            '95000000-0000-0000-0000-000000000011',
            'seller2@todaylunchmenu.com',
            '문구', '노트/다이어리',
            '팬 활동 굿즈 다이어리',
            '일정과 기록을 담는 팬덤 테마의 연간 굿즈 다이어리',
            9800.00, 130, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000012',
            'seller2@todaylunchmenu.com',
            '문구', '노트/다이어리',
            '무드 트래커 굿즈 무선 노트',
            '캐릭터 스탬프 템플릿이 포함된 팬 기록용 굿즈 무선 노트',
            7800.00, 150, 'ACTIVE'
        ),

        -- seller3 (박굿즈) : 문구 > 인쇄물
        (
            '95000000-0000-0000-0000-000000000013',
            'seller3@todaylunchmenu.com',
            '문구', '인쇄물',
            '시그니처 굿즈 포스터',
            '공식 포토 콘셉트를 담은 A3 사이즈 굿즈 포스터',
            12000.00, 100, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000014',
            'seller3@todaylunchmenu.com',
            '문구', '인쇄물',
            '한정판 굿즈 아트프린트',
            '행사 기념 일러스트를 고품질 인쇄한 한정판 굿즈 아트프린트',
            18000.00, 50, 'ACTIVE'
        ),
        -- seller3 (박굿즈) : 의류 > 상의
        (
            '95000000-0000-0000-0000-000000000015',
            'seller3@todaylunchmenu.com',
            '의류', '상의',
            '투데이런치 로고 굿즈 반팔 티셔츠',
            '투데이런치 시그니처 로고를 전면에 배치한 베이직 굿즈 반팔 티셔츠',
            29000.00, 80, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000016',
            'seller3@todaylunchmenu.com',
            '의류', '상의',
            '레트로 콘서트 굿즈 롱슬리브',
            '투어 포스터 감성을 살린 레트로 프린트 굿즈 롱슬리브',
            34000.00, 70, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000017',
            'seller3@todaylunchmenu.com',
            '의류', '상의',
            '로고 자수 굿즈 맨투맨',
            '공식 로고 자수를 넣은 데일리 착용용 굿즈 맨투맨',
            43000.00, 65, 'ACTIVE'
        ),
        -- seller3 (박굿즈) : 의류 > 아우터
        (
            '95000000-0000-0000-0000-000000000018',
            'seller3@todaylunchmenu.com',
            '의류', '아우터',
            '투데이런치 응원 굿즈 후드티',
            '행사 응원용으로 제작한 기모 안감의 오버핏 굿즈 후드티',
            52000.00, 45, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000019',
            'seller3@todaylunchmenu.com',
            '의류', '아우터',
            '컬러블록 굿즈 후드 집업',
            '응원전 테마 컬러를 적용한 배색 디자인의 굿즈 후드 집업',
            59000.00, 38, 'ACTIVE'
        ),

        -- seller4 (이한정) : 의류 > 기타
        (
            '95000000-0000-0000-0000-000000000020',
            'seller4@todaylunchmenu.com',
            '의류', '기타',
            '시그니처 자수 굿즈 볼캡',
            '공식 로고 자수를 넣은 시그니처 굿즈 볼캡',
            21000.00, 85, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000021',
            'seller4@todaylunchmenu.com',
            '의류', '기타',
            '로고 굿즈 양말 3종 세트',
            '팀 컬러 배색과 로고를 적용한 굿즈 양말 3종 세트',
            8900.00, 150, 'ACTIVE'
        ),
        -- seller4 (이한정) : 생활용품 > 주방
        (
            '95000000-0000-0000-0000-000000000022',
            'seller4@todaylunchmenu.com',
            '생활용품', '주방',
            '공식 로고 보온 굿즈 텀블러',
            '행사 참여자용으로 제작한 473ml 보온 스테인리스 굿즈 텀블러',
            18900.00, 60, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000023',
            'seller4@todaylunchmenu.com',
            '생활용품', '주방',
            '슬로건 굿즈 세라믹 머그컵',
            '슬로건 문구를 각인한 350ml 데일리 굿즈 세라믹 머그컵',
            9900.00, 140, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000024',
            'seller4@todaylunchmenu.com',
            '생활용품', '주방',
            '포토 프린트 굿즈 내열 머그컵',
            '아티스트 포토 프린트를 더한 400ml 내열 유리 굿즈 머그컵',
            13200.00, 0, 'SOLD_OUT'
        ),
        -- seller4 (이한정) : 생활용품 > 인테리어
        (
            '95000000-0000-0000-0000-000000000025',
            'seller4@todaylunchmenu.com',
            '생활용품', '인테리어',
            '마스코트 굿즈 쿠션',
            '공식 마스코트 패턴을 자수로 구현한 굿즈 쿠션',
            24000.00, 45, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000026',
            'seller4@todaylunchmenu.com',
            '생활용품', '인테리어',
            '시그니처 굿즈 캔버스 액자',
            '공식 일러스트를 고품질 캔버스에 인쇄한 굿즈 액자',
            32000.00, 30, 'ACTIVE'
        ),

        -- seller5 (정마켓) : 생활용품 > 기타 생활
        (
            '95000000-0000-0000-0000-000000000027',
            'seller5@todaylunchmenu.com',
            '생활용품', '기타 생활',
            '캐릭터 굿즈 마스킹테이프 3종',
            '캐릭터 일러스트 패턴의 다이어리 꾸미기용 굿즈 마스킹테이프 3종 세트',
            6900.00, 110, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000028',
            'seller5@todaylunchmenu.com',
            '생활용품', '기타 생활',
            '로고 굿즈 손거울',
            '핸드백에 넣기 좋은 휴대용 공식 로고 굿즈 손거울',
            11000.00, 75, 'ACTIVE'
        ),
        -- seller5 (정마켓) : 디지털/전자 > 모바일
        (
            '95000000-0000-0000-0000-000000000029',
            'seller5@todaylunchmenu.com',
            '디지털/전자', '모바일',
            '캐릭터 굿즈 투명 폰케이스',
            '캐릭터 일러스트를 담은 아이폰 15 호환 투명 굿즈 폰케이스',
            16000.00, 70, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000030',
            'seller5@todaylunchmenu.com',
            '디지털/전자', '모바일',
            '로고 굿즈 그립톡',
            '공식 로고 일러스트를 새긴 스마트폰 거치 굿즈 그립톡',
            9800.00, 120, 'ACTIVE'
        ),
        -- seller5 (정마켓) : 디지털/전자 > PC 주변기기
        (
            '95000000-0000-0000-0000-000000000031',
            'seller5@todaylunchmenu.com',
            '디지털/전자', 'PC 주변기기',
            '로고 굿즈 마우스패드 XL',
            '미끄럼 방지 처리된 공식 로고 굿즈 마우스패드 (XL)',
            14000.00, 55, 'ACTIVE'
        ),
        (
            '95000000-0000-0000-0000-000000000032',
            'seller5@todaylunchmenu.com',
            '디지털/전자', 'PC 주변기기',
            '캐릭터 굿즈 키보드 스킨',
            '기계식 키보드 호환 캐릭터 프린팅 굿즈 키보드 스킨',
            18000.00, 40, 'ACTIVE'
        ),
        -- seller5 (정마켓) : 디지털/전자 > 기타
        (
            '95000000-0000-0000-0000-000000000033',
            'seller5@todaylunchmenu.com',
            '디지털/전자', '기타',
            '시그니처 굿즈 보조배터리 10000mAh',
            '공식 로고 각인의 10000mAh 대용량 굿즈 보조배터리',
            34000.00, 35, 'ACTIVE'
        )
) AS seed(product_id, seller_email, parent_name, category_name, title, description, price, stock_quantity, status)
JOIN member.member seller_member
    ON seller_member.email = seed.seller_email
JOIN target_categories
    ON target_categories.parent_name   = seed.parent_name
   AND target_categories.category_name = seed.category_name
WHERE NOT EXISTS (
    SELECT 1
    FROM product.product existing
    WHERE existing.seller_id  = seller_member.member_id
      AND existing.deleted_at IS NULL
      AND existing.title       = seed.title
);
