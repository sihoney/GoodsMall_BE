-- ============================================
-- Product Seed Data (개발용) - 굿즈 샵
-- ============================================
-- seller_id = '22222222-2222-2222-2222-222222222202' (이판매)
-- 대분류별 3개씩, 총 9개 상품
-- ============================================

INSERT INTO product.product (product_id, seller_id, category_id, title, description, price, stock_quantity, status, type, view_count, created_at, updated_at)
VALUES
    -- 의류 (반팔 티셔츠 / 후드맨투맨 / 볼캡)
    ('dddddddd-dddd-dddd-dddd-ddddddddd001', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '곰돌이 반팔 티셔츠', '귀여운 곰돌이 프린팅 반팔 티셔츠 (화이트)', 25000.00, 100, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd002', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '로고 오버핏 후드', '브랜드 로고 자수 오버핏 후드 (블랙)', 45000.00, 50, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd003', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '데일리 볼캡', '심플 자수 로고 볼캡 (베이지)', 18000.00, 80, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    -- 문구 (미니노트 / 미니노트 / 캐릭터 스티커)
    ('dddddddd-dddd-dddd-dddd-ddddddddd004', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '감성 미니노트 3종 세트', '파스텔 컬러 미니노트 3권 세트 (A6)', 8900.00, 200, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd005', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '고양이 스티커팩', '고양이 일러스트 스티커 20매입', 3500.00, 300, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd006', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002',
     '꽃 데코 스티커팩', '수채화 꽃 데코 스티커 15매입', 4200.00, 0, 'SOLD_OUT', 'GENERAL', 0, NOW(), NOW()),

    -- 액세서리 (아크릴 키링 / 아크릴 키링 / 하드케이스)
    ('dddddddd-dddd-dddd-dddd-ddddddddd007', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '별빛 아크릴 키링', '반짝이 별 모양 아크릴 키링', 6500.00, 150, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd008', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '달 아크릴 키링', '초승달 모양 아크릴 키링', 6500.00, 120, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    ('dddddddd-dddd-dddd-dddd-ddddddddd009', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003',
     '무지 하드 폰케이스', '투명 무지 하드케이스 (아이폰 15)', 12000.00, 60, 'ACTIVE', 'GENERAL', 0, NOW(), NOW()),

    -- 경매 상품 (테스트용)
    ('dddddddd-dddd-dddd-dddd-ddddddddd010', '22222222-2222-2222-2222-222222222202', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001',
     '한정판 콜라보 후드 (경매)', '브랜드 한정판 콜라보 후드 경매 상품', 50000.00, 1, 'ACTIVE', 'AUCTION', 0, NOW(), NOW())
ON CONFLICT (product_id) DO NOTHING;
