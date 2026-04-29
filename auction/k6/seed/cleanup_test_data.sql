-- ============================================================
-- 부하테스트 데이터 전체 정리 (테스트 완료 후 실행)
-- ============================================================
-- 실행 방법:
--   kubectl exec -it <postgres-pod> -n goods-mall -- \
--     psql -U postgres -d goods_mall -f /path/to/cleanup_test_data.sql
--
-- 실행 순서:
--   1) ROLLBACK 상태로 실행 → STEP 0 SELECT 결과 확인
--   2) 예상 범위가 맞으면 마지막 ROLLBACK → COMMIT 으로 변경 후 재실행
-- ============================================================

BEGIN;

-- ============================================================
-- [STEP 0] 삭제 전 범위 확인 (SELECT only)
-- ============================================================

SELECT '=== 정리 대상 경매 ===' AS section;
SELECT auction_id, product_title, status, current_highest_price
FROM auction.auction
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

SELECT '=== 입찰 건수 ===' AS section;
SELECT COUNT(*) AS bid_count
FROM auction.bid
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

SELECT '=== auction_deposit (실제 멤버 입찰만 존재) ===' AS section;
SELECT COUNT(*) AS deposit_count, SUM(deposit_amount) AS total_held
FROM payment.auction_deposit
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

SELECT '=== wallet 영향 범위 (복원 대상) ===' AS section;
SELECT w.member_id,
       w.balance                       AS current_balance,
       COUNT(wt.transaction_id)        AS test_tx_count,
       SUM(CASE WHEN wt.transaction_id IN (
               SELECT hold_wallet_transaction_id
               FROM payment.auction_deposit
               WHERE auction_id IN (
                   'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
               )
           ) THEN wt.amount ELSE 0 END) AS total_hold_amount,
       SUM(CASE WHEN wt.transaction_id IN (
               SELECT refund_wallet_transaction_id
               FROM payment.auction_deposit
               WHERE auction_id IN (
                   'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
                   'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
               )
               AND refund_wallet_transaction_id IS NOT NULL
           ) THEN wt.amount ELSE 0 END) AS total_refund_amount
FROM payment.wallet w
JOIN payment.wallet_transaction wt ON wt.wallet_id = w.wallet_id
WHERE wt.transaction_id IN (
    SELECT hold_wallet_transaction_id
    FROM payment.auction_deposit
    WHERE auction_id IN (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
    )
)
GROUP BY w.member_id, w.balance;

-- ============================================================
-- STEP 0 결과 확인 후 아래 DELETE 진행
-- ============================================================

-- ============================================================
-- [STEP 1] 테스트 wallet_transaction ID 수집 (임시 테이블)
-- hold + refund(환불) 트랜잭션 모두 포함
-- ============================================================

CREATE TEMP TABLE _test_wallet_tx_ids ON COMMIT DROP AS
SELECT hold_wallet_transaction_id AS tx_id
FROM payment.auction_deposit
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
)
UNION
SELECT refund_wallet_transaction_id
FROM payment.auction_deposit
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
)
AND refund_wallet_transaction_id IS NOT NULL;

-- ============================================================
-- [STEP 2] wallet balance 복원
--
-- 원리: 테스트 트랜잭션을 제외한 마지막 balance_after 로 복원
--   - OUTBID 입찰: hold(차감) + refund(환불) 둘 다 삭제 → net 0, 변화 없어야 함
--   - 낙찰 중 입찰: hold(차감)만 삭제 → 차감분 복원
-- balance_after 기준 복원이 두 케이스를 자동으로 처리함
--
-- 주의: 테스트 트랜잭션 외 다른 거래가 없는 wallet 은
--       마지막 balance_after 가 없으므로 시드 초기값(1,000,000)으로 복원
-- ============================================================

UPDATE payment.wallet w
SET balance    = COALESCE(
                     -- 테스트 트랜잭션 제외한 마지막 잔액
                     (SELECT wt.balance_after
                      FROM payment.wallet_transaction wt
                      WHERE wt.wallet_id = w.wallet_id
                        AND wt.tx_id NOT IN (SELECT tx_id FROM _test_wallet_tx_ids)
                      ORDER BY wt.created_at DESC
                      LIMIT 1),
                     -- 다른 거래 내역이 없으면 시드 초기값으로 복원
                     1000000.00
                 ),
    updated_at = NOW()
WHERE w.wallet_id IN (
    SELECT DISTINCT wt.wallet_id
    FROM payment.wallet_transaction wt
    WHERE wt.transaction_id IN (SELECT tx_id FROM _test_wallet_tx_ids)
);

-- ============================================================
-- [STEP 3] payment.wallet_transaction 삭제
-- ============================================================

DELETE FROM payment.wallet_transaction
WHERE transaction_id IN (SELECT tx_id FROM _test_wallet_tx_ids);

-- ============================================================
-- [STEP 4] payment.auction_deposit 삭제
-- ============================================================

DELETE FROM payment.auction_deposit
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

-- ============================================================
-- [STEP 5] payment.outbox_events 삭제
-- ============================================================

DELETE FROM payment.outbox_events
WHERE aggregate_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'::uuid
)
OR aggregate_id IN (
    SELECT bid_id FROM auction.bid
    WHERE auction_id IN (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
    )
);

-- ============================================================
-- [STEP 6] auction.outbox_event 삭제
-- ============================================================

DELETE FROM auction.outbox_event
WHERE aggregate_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105'::uuid,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'::uuid
)
OR aggregate_id IN (
    SELECT bid_id FROM auction.bid
    WHERE auction_id IN (
        'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
    )
);

-- ============================================================
-- [STEP 7] auction.bid 삭제 (FK: auction_id → auction)
-- ============================================================

DELETE FROM auction.bid
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

-- ============================================================
-- [STEP 8] auction.auction 삭제
-- ============================================================

DELETE FROM auction.auction
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

-- ============================================================
-- [STEP 9] 삭제 후 검증
-- ============================================================

SELECT '=== 삭제 후 잔여 확인 (모두 0이어야 정상) ===' AS section;

SELECT 'auction' AS tbl, COUNT(*) AS remaining
FROM auction.auction
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
)
UNION ALL
SELECT 'bid', COUNT(*)
FROM auction.bid
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
)
UNION ALL
SELECT 'auction_deposit', COUNT(*)
FROM payment.auction_deposit
WHERE auction_id IN (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201'
);

SELECT '=== wallet 잔액 복원 결과 ===' AS section;
SELECT member_id, balance
FROM payment.wallet
WHERE member_id IN (
    '11111111-1111-1111-1111-111111111101',
    '11111111-1111-1111-1111-111111111102',
    '11111111-1111-1111-1111-111111111103',
    '22222222-2222-2222-2222-222222222202'
);

-- ============================================================
-- STEP 9 결과가 모두 0이고 wallet 잔액이 정상이면
-- 아래 ROLLBACK 을 COMMIT 으로 변경하여 재실행
-- ============================================================

ROLLBACK;
-- COMMIT;
