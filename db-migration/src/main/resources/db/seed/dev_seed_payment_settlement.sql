-- 개발/테스트 전용 seed 스크립트입니다.
-- Flyway 적용이 끝난 뒤, 로컬 검증에 샘플 데이터가 필요할 때만 수동 실행해 주세요.
-- 운영 배포 migration 경로에는 이 파일을 포함하지 않습니다.

INSERT INTO payment.wallet (wallet_id, member_id, balance, updated_at, created_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     '11111111-1111-1111-1111-111111111101',
     100000,
     NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
     '22222222-2222-2222-2222-222222222202',
     0,
     NOW(), NOW()),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc3',
     '33333333-3333-3333-3333-333333333303',
     50000,
     NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;

INSERT INTO payment.charge (
    charge_id, member_id, wallet_id,
    requested_amount, approved_amount,
    toss_bank_code, pg_order_id, pg_payment_key,
    charge_status, requested_at, approved_at, failed_at, failure_reason
)
VALUES
    ('dd000001-0000-0000-0000-000000000001',
     '11111111-1111-1111-1111-111111111101',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     100000, 100000,
     '92', 'test-pg-order-001', 'test_payment_key_001',
     'CONFIRM_SUCCESS',
     NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NULL, NULL),
    ('dd000002-0000-0000-0000-000000000002',
     '11111111-1111-1111-1111-111111111101',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     50000, 50000,
     '20', 'test-pg-order-002', 'test_payment_key_002',
     'CONFIRM_SUCCESS',
     NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NULL, NULL),
    ('dd000003-0000-0000-0000-000000000003',
     '11111111-1111-1111-1111-111111111101',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     30000, NULL,
     NULL, 'test-pg-order-003', NULL,
     'CONFIRM_FAILED',
     NOW() - INTERVAL '3 days', NULL, NOW() - INTERVAL '3 days', 'PG approval failed'),
    ('dd000004-0000-0000-0000-000000000004',
     '11111111-1111-1111-1111-111111111101',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     20000, NULL,
     NULL, 'test-pg-order-004', NULL,
     'PENDING',
     NOW(), NULL, NULL, NULL)
ON CONFLICT (pg_order_id) DO NOTHING;

INSERT INTO payment.wallet_transaction (
    transaction_id, wallet_id, amount, balance_after,
    transaction_type, reference_id, reference_type, description, created_at
)
VALUES
    ('ee000001-0000-0000-0000-000000000001',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     100000, 150000,
     'CHARGE',
     'dd000001-0000-0000-0000-000000000001', 'CHARGE',
     'Toss charge 100000',
     NOW() - INTERVAL '2 days'),
    ('ee000002-0000-0000-0000-000000000002',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     50000, 150000,
     'CHARGE',
     'dd000002-0000-0000-0000-000000000002', 'CHARGE',
     'Toss charge 50000',
     NOW() - INTERVAL '1 day'),
    ('ee000003-0000-0000-0000-000000000003',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
     -50000, 100000,
     'PURCHASE',
     'ff000001-0000-0000-0000-000000000001', 'ORDER',
     'Order purchase 50000',
     NOW() - INTERVAL '12 hours')
ON CONFLICT (transaction_id) DO NOTHING;

INSERT INTO payment.escrow (
    escrow_id, order_id,
    buyer_member_id, seller_member_id,
    amount, original_amount, refunded_amount,
    escrow_status,
    refunded_at, released_at,
    reference_id, reference_type,
    created_at, updated_at
)
VALUES
    ('ee000011-0000-0000-0000-000000000011',
     'ff000001-0000-0000-0000-000000000001',
     '11111111-1111-1111-1111-111111111101',
     '22222222-2222-2222-2222-222222222202',
     50000, 50000, 0,
     'HELD',
     NULL, NULL,
     'ff000001-0000-0000-0000-000000000001', 'ORDER',
     NOW() - INTERVAL '12 hours', NULL),
    ('ee000012-0000-0000-0000-000000000012',
     'ff000002-0000-0000-0000-000000000002',
     '11111111-1111-1111-1111-111111111101',
     '22222222-2222-2222-2222-222222222202',
     80000, 80000, 0,
     'RELEASED',
     NULL, NOW() - INTERVAL '5 days',
     'ff000002-0000-0000-0000-000000000002', 'ORDER',
     NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days'),
    ('ee000013-0000-0000-0000-000000000013',
     'ff000003-0000-0000-0000-000000000003',
     '11111111-1111-1111-1111-111111111101',
     '22222222-2222-2222-2222-222222222202',
     120000, 120000, 0,
     'RELEASED',
     NULL, NOW() - INTERVAL '35 days',
     'ff000003-0000-0000-0000-000000000003', 'ORDER',
     NOW() - INTERVAL '40 days', NOW() - INTERVAL '35 days'),
    ('ee000014-0000-0000-0000-000000000014',
     'ff000004-0000-0000-0000-000000000004',
     '11111111-1111-1111-1111-111111111101',
     '22222222-2222-2222-2222-222222222202',
     30000, 30000, 30000,
     'REFUNDED',
     NOW() - INTERVAL '2 days', NULL,
     'ff000004-0000-0000-0000-000000000004', 'ORDER',
     NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days')
ON CONFLICT (escrow_id) DO NOTHING;

INSERT INTO settlement.settlement_item (
    settlement_item_id, settlement_id, settlement_item_status,
    order_id, escrow_id, seller_id,
    gross_amount, fee_amount, net_amount,
    released_at, created_at
)
VALUES
    ('aa100001-0000-0000-0000-000000000001',
     NULL, 'UNASSIGNED',
     'ff000001-0000-0000-0000-000000000001',
     'ee000011-0000-0000-0000-000000000011',
     '22222222-2222-2222-2222-222222222202',
     50000, 5000, 45000,
     NOW() - INTERVAL '12 hours', NOW() - INTERVAL '11 hours'),
    ('aa100002-0000-0000-0000-000000000002',
     'aa200001-0000-0000-0000-000000000001', 'ASSIGNED',
     'ff000002-0000-0000-0000-000000000002',
     'ee000012-0000-0000-0000-000000000012',
     '22222222-2222-2222-2222-222222222202',
     80000, 8000, 72000,
     NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    ('aa100003-0000-0000-0000-000000000003',
     'aa200002-0000-0000-0000-000000000002', 'ASSIGNED',
     'ff000003-0000-0000-0000-000000000003',
     'ee000013-0000-0000-0000-000000000013',
     '22222222-2222-2222-2222-222222222202',
     120000, 12000, 108000,
     NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days')
ON CONFLICT (escrow_id) DO NOTHING;

INSERT INTO settlement.settlement (
    settlement_id, seller_id,
    settlement_year, settlement_month,
    total_sales_amount, fee_amount, final_settlement_amount, settled_amount,
    settlement_status, settled_at, last_failure_reason,
    requested_at, updated_at
)
VALUES
    ('aa200001-0000-0000-0000-000000000001',
     '22222222-2222-2222-2222-222222222202',
     EXTRACT(YEAR FROM NOW())::INT,
     EXTRACT(MONTH FROM NOW())::INT,
     80000, 8000, 72000, 0,
     'PENDING',
     NULL, NULL,
     NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    ('aa200002-0000-0000-0000-000000000002',
     '22222222-2222-2222-2222-222222222202',
     EXTRACT(YEAR FROM (NOW() - INTERVAL '1 month'))::INT,
     EXTRACT(MONTH FROM (NOW() - INTERVAL '1 month'))::INT,
     120000, 12000, 108000, 108000,
     'COMPLETED',
     NOW() - INTERVAL '28 days', NULL,
     NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days'),
    ('aa200003-0000-0000-0000-000000000003',
     '22222222-2222-2222-2222-222222222202',
     EXTRACT(YEAR FROM (NOW() - INTERVAL '2 months'))::INT,
     EXTRACT(MONTH FROM (NOW() - INTERVAL '2 months'))::INT,
     60000, 6000, 54000, 0,
     'FAILED',
     NULL, 'WALLET_NOT_FOUND',
     NOW() - INTERVAL '60 days', NOW() - INTERVAL '58 days')
ON CONFLICT (settlement_id) DO NOTHING;
