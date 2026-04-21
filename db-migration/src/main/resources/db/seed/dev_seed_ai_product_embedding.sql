-- ============================================
-- AI Product Embedding Seed Data (개발용)
-- ============================================
-- 기존 dev_seed_product.sql 상품과 AI 추천 테스트 전용 추가 상품의 임베딩입니다.
-- Swagger 추천 테스트 기준 상품:
--   dddddddd-dddd-dddd-dddd-ddddddddd001
-- ============================================

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
VALUES
    ('dddddddd-dddd-dddd-dddd-ddddddddd101', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '베이직 화이트 티셔츠', '데일리로 입기 좋은 베이직 화이트 반팔 티셔츠', 22000.00, 120, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd102', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '캐릭터 프린팅 티셔츠', '전면 캐릭터 프린팅이 들어간 코튼 반팔 티셔츠', 27000.00, 90, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd103', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '컬러 포인트 티셔츠', '소매 배색 포인트가 있는 루즈핏 티셔츠', 29000.00, 70, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd104', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '자수 로고 맨투맨', '작은 자수 로고가 들어간 코튼 맨투맨', 39000.00, 65, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd105', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '오버핏 집업 후드', '간절기에 입기 좋은 오버핏 집업 후드', 52000.00, 45, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd106', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '빈티지 워싱 볼캡', '워싱 처리된 빈티지 무드의 볼캡', 21000.00, 85, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd107', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '스몰 로고 버킷햇', '작은 로고 자수가 들어간 코튼 버킷햇', 24000.00, 55, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd108', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '데일리 에코백', '가볍게 들기 좋은 캔버스 에코백', 19000.00, 110, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd201', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '라인 미니노트 세트', '줄노트와 무지노트가 함께 들어 있는 미니노트 세트', 7900.00, 180, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd202', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '체크 메모패드', '체크 패턴 커버의 떡메모지', 4500.00, 220, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd203', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '데코 마스킹테이프', '다이어리 꾸미기에 좋은 일러스트 마스킹테이프', 3800.00, 260, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd204', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '캐릭터 젤펜 3종', '부드럽게 써지는 캐릭터 젤펜 3종 세트', 6200.00, 150, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd205', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '플래너 스티커 세트', '월간 플래너에 붙이기 좋은 일정 스티커', 5200.00, 240, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd206', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '감성 엽서 5종', '따뜻한 색감의 일러스트 엽서 5종 세트', 6900.00, 130, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd207', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '투명 포토카드 홀더', '포토카드를 보관하기 좋은 투명 홀더', 3500.00, 300, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd208', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '미니 파일 바인더', '스티커와 메모지를 정리하기 좋은 미니 바인더', 8900.00, 95, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd301', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '하트 아크릴 키링', '하트 모양 투명 아크릴 키링', 6800.00, 170, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd302', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '캐릭터 러버 키링', '말랑한 소재의 캐릭터 러버 키링', 7500.00, 140, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd303', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '스마트톡 그립', '휴대폰에 부착하는 캐릭터 스마트톡', 9800.00, 125, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd304', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '투명 젤리 폰케이스', '일러스트 카드와 함께 쓰기 좋은 투명 젤리 케이스', 11000.00, 80, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd305', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '캔버스 파우치', '작은 소품을 정리하기 좋은 캔버스 파우치', 13500.00, 75, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd306', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '메탈 뱃지 세트', '가방이나 옷에 달기 좋은 메탈 뱃지 2종', 8200.00, 160, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd307', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '아크릴 스탠드', '책상 위에 세워두는 캐릭터 아크릴 스탠드', 12500.00, 100, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),
    ('dddddddd-dddd-dddd-dddd-ddddddddd308', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '미니 랜야드 스트랩', '키링과 함께 쓰기 좋은 미니 랜야드 스트랩', 5900.00, 190, 'ACTIVE', 'GENERAL', 0, NOW(), NOW())
ON CONFLICT (product_id) DO UPDATE
SET title = EXCLUDED.title,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    stock_quantity = EXCLUDED.stock_quantity,
    status = EXCLUDED.status,
    type = EXCLUDED.type,
    updated_at = NOW();

WITH seed_embeddings(embedding_id, product_id, clothing_score, stationery_score, accessory_score) AS (
    VALUES
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee001'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd001'::uuid, 0.900, 0.100, 0.050),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee002'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd002'::uuid, 0.880, 0.120, 0.060),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee003'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd003'::uuid, 0.720, 0.180, 0.220),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee004'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd004'::uuid, 0.080, 0.900, 0.060),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee005'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd005'::uuid, 0.060, 0.880, 0.080),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee006'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd006'::uuid, 0.050, 0.820, 0.120),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee007'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd007'::uuid, 0.120, 0.100, 0.900),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee008'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd008'::uuid, 0.100, 0.090, 0.880),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee009'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd009'::uuid, 0.160, 0.120, 0.720),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee010'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd010'::uuid, 0.860, 0.110, 0.070),

        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee101'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd101'::uuid, 0.910, 0.090, 0.040),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee102'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd102'::uuid, 0.895, 0.100, 0.050),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee103'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd103'::uuid, 0.870, 0.110, 0.060),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee104'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd104'::uuid, 0.840, 0.120, 0.070),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee105'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd105'::uuid, 0.820, 0.100, 0.080),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee106'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd106'::uuid, 0.700, 0.150, 0.260),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee107'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd107'::uuid, 0.680, 0.140, 0.290),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee108'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd108'::uuid, 0.480, 0.180, 0.520),

        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee201'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd201'::uuid, 0.070, 0.920, 0.050),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee202'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd202'::uuid, 0.060, 0.900, 0.060),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee203'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd203'::uuid, 0.050, 0.870, 0.100),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee204'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd204'::uuid, 0.070, 0.850, 0.090),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee205'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd205'::uuid, 0.050, 0.890, 0.080),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee206'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd206'::uuid, 0.090, 0.780, 0.170),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee207'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd207'::uuid, 0.080, 0.700, 0.300),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee208'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd208'::uuid, 0.080, 0.820, 0.180),

        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee301'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd301'::uuid, 0.100, 0.080, 0.920),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee302'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd302'::uuid, 0.090, 0.070, 0.900),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee303'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd303'::uuid, 0.120, 0.100, 0.840),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee304'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd304'::uuid, 0.140, 0.110, 0.800),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee305'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd305'::uuid, 0.260, 0.150, 0.680),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee306'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd306'::uuid, 0.110, 0.090, 0.860),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee307'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd307'::uuid, 0.090, 0.160, 0.760),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeee308'::uuid, 'dddddddd-dddd-dddd-dddd-ddddddddd308'::uuid, 0.120, 0.090, 0.880)
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
    embedding_id,
    product_id,
    CAST(
            '['
            || clothing_score || ','
            || stationery_score || ','
            || accessory_score || ','
            || rtrim(repeat('0.001,', 1533), ',')
            || ']'
            AS vector
    ),
    NOW(),
    true,
    NOW(),
    NOW()
FROM seed_embeddings
ON CONFLICT (product_id) DO UPDATE
SET embedding = EXCLUDED.embedding,
    source_updated_at = EXCLUDED.source_updated_at,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
