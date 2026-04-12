# payment 카드결제 confirm 연동 계약 초안

## 목적

- `payment` 서비스에 Toss 카드결제 승인 이력 저장 구조를 추가한다.
- `order` 서비스와 프론트는 이 문서를 기준으로 `payment` confirm API 연동 계약을 맞춘다.
- 현재 단계에서는 `payment`가 `wallet_transaction`을 건드리지 않는다.

## 책임 분리

- 프론트
  - `order`에 주문 생성 요청
  - `PENDING` 주문 응답 수신
  - Toss 위젯 호출
  - Toss success 결과의 `paymentKey`, `orderId`, `amount`를 이용해 `payment` confirm API 호출
- `payment`
  - Toss confirm API 호출
  - 카드 승인 성공/실패 이력을 `card_transaction`에 append-only로 저장
  - 승인 결과와 `transactionGroupId`를 응답으로 제공
- `order`
  - 주문 생성 및 상태 관리
  - `payment` confirm 성공 이후 주문 확정/후속 상태 전이 처리

## card_transaction 저장 모델

- 대표 row는 두지 않는다.
- 하나의 카드 승인 건은 하나의 `transactionGroupId`로 묶인다.
- 같은 승인 건에 속한 각 `orderItemId`별로 row를 1건씩 저장한다.
- 이후 부분 취소/환불이 생기면 기존 row를 수정하지 않고 새 row를 추가한다.
- 취소/환불 row는 `relatedTransactionId`로 원본 `PAYMENT` row를 참조한다.

## payment confirm API 초안

- Path: `POST /api/payments/card/confirm`
- 인증: 미정
  - 현재 payment의 충전 confirm은 무인증이다.
  - 카드결제 confirm은 프론트 직접 호출이므로 인증/위변조 검증 정책 합의가 필요하다.

## 요청 필드 초안

```json
{
  "buyerId": "UUID",
  "orderId": "UUID",
  "paymentKey": "toss payment key",
  "amount": 12000,
  "orderItems": [
    {
      "orderItemId": "UUID",
      "amount": 6000
    },
    {
      "orderItemId": "UUID",
      "amount": 6000
    }
  ]
}
```

## 요청 필드 설명

- `buyerId`
  - 카드 결제 주체 식별자
  - `card_transaction.member_id`에 저장
- `orderId`
  - Toss 측 merchant 주문 식별값과 매핑되는 주문 식별자
- `paymentKey`
  - Toss success 응답에서 받은 값
- `amount`
  - 전체 승인 금액
- `orderItems`
  - `card_transaction.reference_id`에 적재할 주문상품 기준 목록
  - 각 항목의 `amount` 합은 `amount`와 같아야 한다

## 응답 필드 초안

```json
{
  "success": true,
  "data": {
    "transactionGroupId": "UUID",
    "orderId": "UUID",
    "buyerId": "UUID",
    "amount": 12000,
    "status": "SUCCESS",
    "approvedAt": "2026-04-12T14:30:00"
  },
  "error": null
}
```

## order 서비스에 필요한 후속 처리

- `payment` confirm 성공 시 `order`는 아래 정보를 이용해 자체 상태를 전이해야 한다.
  - `transactionGroupId`
  - `orderId`
  - `buyerId`
  - `amount`
  - `status`
  - `approvedAt`
- `payment`는 주문 상태를 직접 확정하지 않는다.
- `order`는 `payment` confirm 성공 결과를 기준으로 주문 확정/후속 배송 프로세스를 이어간다.

## 추후 커밋 예정 범위

- `POST /api/payments/card/confirm` API 구현
- Toss confirm 결과를 기준으로 `card_transaction` PENDING -> SUCCESS/FAILED 반영
- 부분 취소/환불용 `CANCEL`, `REFUND` row 추가 로직
