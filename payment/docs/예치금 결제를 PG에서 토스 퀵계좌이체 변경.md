# 개요
- 예치금 결제 및 환불을 PG사에서 토스 결제 시스템으로 변경하는 작업입니다.
- PG사에서 예치금 결제 및 환불을 처리하던 것을 토스 결제 시스템으로 이전하여, 결제 및 환불 프로세스를 개선하고 사용자 경험을 향상시키는 것을 목표로 합니다.

# 기존 문제점
- 예치금을 PG사를 통해서 입금시 환불 처리시 복잡성이 증가 
- 물품 구매 과정을 PG 방식을 통해 취소 / 환불을 일관되게 진행할려고 하였으나, 예치금 방식은 기능 요구사항이므로 제거할 수 없는 상황
- 예치금을 현금과 동일하게 만들기 위하여 계좌이체 방식을 진행할려고 했으나 실제 서비스가 아니므로 계좌가 없어 이 기능을 구현하기 어려움

# 해결 방안
- 토스 PG를 통한 계좌이체 방식을 진행할려고 함
- 카드 결제 등 다른 결제 수단도 지원하여 목표에 어울리지 않음
- 확인한 결과 CI용 퀵계좌이체 방식을 Toss에서 지원하여 이를 도입하기로 함

# 결제 흐름
1. 사용자가 예치금 금액을 선택
2. 예치금 충전 버튼을 클릭
3. 프론트에서 `/api/payments/charge`로 POST 요청을 보냄
4. 백엔드에서 예치금 충전 요청을 처리하고 프론트에 orderId를 보낸다.
5. orderId를 받은 프론트는 토스 결제 시스템으로 리디렉션하여 결제를 진행
6. 결제가 진행되면 성공/ 실패에 따라 successUrl 또는 failUrl로 리디렉션

### 성공
1. 결제 성공시 백엔드에서 프론트에서 전달한 url에 있는 쿼리스트링을 이용하여 결제 확인을 위해 toss에 요청을 보냄
2. 결제 확인이 성공하면 Toss 응답의 `method`가 `계좌이체`인지 확인하고, `transfer.bankCode`를 `Charge.tossBankCode`에 저장
3. 프론트에게 응답을 보냄

### 실패
1. 결제 실패시 실패 사유 및 실패 시간 DB 저장
2. 프론트에게 결제 실패 응답을 보냄

# API 응답시 필요한 데이터
### 예치금 충전 API 응답
- 필수값:
    - `amount`: 양수

- 요청 예시:
```json
{
  "amount": 5000
}
```

- 응답 데이터 필드:
    - `chargeId`
    - `walletId`
    - `pgOrderId`
    - `amount`
    - `chargeStatus`

- 응답 예시:
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
### 토스 결제 확인 API 응답
- 필수값:
    - `chargeId`
    - `paymentKey`
    - `orderId`
    - `amount`

- 응답 데이터 필드:
    - `chargeId`
    - `chargeStatus`
    - `approvedAmount`
    - `walletBalance`
    - `approvedAt`

- 참고:
    - `tossBankCode`는 생성 API에서 받지 않는다.
    - `POST /api/payments/confirm` 처리 중 Toss `Payment` 응답의 `method == "계좌이체"`일 때만 `transfer.bankCode`를 저장한다.
    - 이후 충전 목록/상세 조회에서 `tossBankCode`를 내려준다.

# 변경점
- ERD) Charge 테이블에서 `pg_provider`(PG 식별자) 제거
- ERD) Charge 테이블에서 `toss_bank_code`(토스 은행 코드) 추가
- API) 예치금 충전 생성 응답에서 `pgProvider` 제거
- API) 충전 목록/상세 조회 응답에서 `pgProvider` 제거 후 `tossBankCode` 추가
- API) `tossBankCode`는 confirm 시점에만 저장

# 참고 문서
- [Toss 퀵계좌이체 연동하기](https://docs.tosspayments.com/guides/payment/integration-quick#1-%EA%B2%B0%EC%A0%9C%EC%B0%BD-%EB%9D%84%EC%9A%B0%EA%B8%B0)
- [Toss 코어 API](https://docs.tosspayments.com/reference)
