-- 활성 ReturnRequest(검수 대기/진행 중)가 있는 OrderItem 중 아직 DELIVERED 상태로 남아있는
-- 항목을 RETURN_REQUESTED로 동기화한다. V61 이전에 만들어진 데이터의 정합성을 맞추기 위함.
-- 활성 상태 = FAILED(거절)을 제외한 모든 상태 (REQUESTED, PICKUP_REQUESTED, PICKED_UP, RECEIVED, COMPLETED)

UPDATE order_service.order_items oi
SET order_item_status = 'RETURN_REQUESTED',
    updated_at = NOW()
WHERE oi.order_item_status = 'DELIVERED'
  AND EXISTS (
      SELECT 1
      FROM order_service.return_requests rr
      WHERE rr.order_item_id = oi.order_item_id
        AND rr.status <> 'FAILED'
  );
