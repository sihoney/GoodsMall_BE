# 개요

- 이 문서는 예치금 충전 방식을 PG 연동 기준에서 Toss 계좌이체 중심으로 정리했던 변경 배경 문서입니다.
- 현재 코드 기준 기능 설명 문서라기보다, 충전 구조 변경 배경과 응답 필드 변경 이력을 설명하는 문서로 보는 것이 맞습니다.

## 문서 수정 메모

기존 문서에는 `예치금 결제 및 환불`이라는 표현이 반복되었는데, 현재 payment 코드 기준으로 charge 전용 환불 기능이 운영 기능처럼 남아 있다고 보기 어렵습니다.

현재 코드 기준으로 보면:

- 예치금 충전 기능은 `charge` 기반으로 유지됩니다.
- 주문 환불은 `PaymentRefund` 흐름에서 처리됩니다.
- 예치금 출금은 별도 `withdraw_request` 흐름으로 처리됩니다.
- 따라서 이 문서에서 말하는 `환불`은 현재 운영 기능 설명이 아니라 과거 구조 변경 배경으로 이해해야 합니다.

# 기존 문제점

- 예치금 충전을 PG로 처리하면 충전 흐름과 주문 환불 흐름의 경계가 복잡해질 수 있었습니다.
- 예치금은 주문 환불과 동일한 구조로 가져가기 어려웠습니다.
- 실제 서비스에서는 계좌 기반 충전 흐름이 더 단순한 방향이었습니다.

# 해결 방향

- Toss 결제 시스템을 이용해 예치금 충전 흐름을 단순화했습니다.
- 충전 생성과 승인 확인을 분리해 처리했습니다.
- 현재는 충전, 주문 환불, 예치금 출금을 서로 다른 흐름으로 구분해서 보는 것이 맞습니다.

# 결제 흐름

1. 사용자가 예치금 금액을 선택합니다.
2. 프론트에서 `/api/payments/charge`로 POST 요청을 보냅니다.
3. 백엔드는 예치금 충전 요청을 처리하고 프론트에 `orderId`를 보냅니다.
4. 프론트는 Toss 결제창으로 이동합니다.
5. 성공 시 `POST /api/payments/confirm`으로 승인 확인을 요청합니다.

### 성공

1. 결제 성공 후 프론트는 백엔드 확인 API를 호출합니다.
2. 백엔드는 Toss 응답의 `method`가 계좌이체인지 확인합니다.
3. 계좌이체라면 `transfer.bankCode`를 `Charge.tossBankCode`에 저장합니다.
4. 이후 충전 목록, 충전 상세 조회에서 `tossBankCode`를 내려줍니다.

### 실패

1. 결제 실패 사유와 실패 시간을 저장합니다.
2. 프론트에 실패 응답을 전달합니다.

# API 응답에서 필요한 데이터

### 예치금 충전 API 응답

- 필수값
  - `amount`

요청 예시:

```json
{
  "amount": 5000
}
```

응답 필드:

- `chargeId`
- `walletId`
- `pgOrderId`
- `amount`
- `chargeStatus`

응답 예시:

```json
{
  "success": true,
  "data": {
    "chargeId": "8f8f0ee4-6ef4-47e5-b4aa-c42fd34a8f55",
    "walletId": "f91dfd08-b5e8-4da0-a8ea-1f41c41fd764",
    "pgOrderId": "CHARGE-8f8f0ee4-6ef4-47e5-b4aa-c42fd34a8f55",
    "amount": 5000,
    "chargeStatus": "PENDING"
  },
  "error": null
}
```

### Toss 결제 확인 API 응답

- 필수값
  - `chargeId`
  - `paymentKey`
  - `orderId`
  - `amount`

응답 필드:

- `chargeId`
- `chargeStatus`
- `approvedAmount`
- `walletBalance`
- `approvedAt`

참고:

- `tossBankCode`는 충전 생성 API에서 받지 않습니다.
- `POST /api/payments/confirm` 처리 중 Toss `Payment` 응답의 `method == "계좌이체"`인 경우에만 `transfer.bankCode`를 저장합니다.
- 이후 충전 목록, 충전 상세 조회에서 `tossBankCode`를 내려줍니다.

# 변경점

- ERD: Charge 테이블에서 `pg_provider` 제거
- ERD: Charge 테이블에 `toss_bank_code` 추가
- API: 충전 생성 응답에서 `pgProvider` 제거
- API: 충전 목록, 충전 상세 조회 응답에서 `pgProvider` 제거 후 `tossBankCode` 추가
- 문서 해석 기준: 이 문서는 현재 charge 환불 기능 설명 문서가 아니라 충전 구조 변경 배경 문서입니다.

# 참고 문서

- [Toss 결제 연동 가이드](https://docs.tosspayments.com/guides/payment/integration-quick#1-%EA%B2%B0%EC%A0%9C%EC%B0%BD-%EB%9D%84%EC%9A%B0%EA%B8%B0)
- [Toss 코어 API](https://docs.tosspayments.com/reference)
