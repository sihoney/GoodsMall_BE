# Payment Service

`payment` 모듈은 결제와 지갑을 담당하는 서비스입니다. 사용자 지갑 생성, 충전/충전 확정/환불, 주문 결제, 에스크로 보관, 구매 확정 이후 정산 후보 발행, 판매자 환불, 정산금 입금을 처리합니다.

## 1. 한눈에 보는 역할

이 모듈의 핵심 책임은 아래와 같습니다.

- 회원 생성 시 `wallet`을 만든다.
- 사용자의 충전 요청을 생성하고, Toss Payments 승인 결과를 반영한다.
- 충전 환불 시 PG 취소와 지갑 차감을 함께 처리한다.
- 주문 결제 시 구매자 지갑에서 금액을 차감하고 판매자별 `escrow`를 만든다.
- 구매 확정 이벤트를 받아 `escrow release`를 수행한다.
- `escrow`가 release 되면 settlement 모듈이 사용할 정산 후보 이벤트를 발행한다.
- settlement 모듈의 지급 요청을 받아 판매자 지갑에 정산금을 적립한다.

## 2. 실행 정보

| 항목 | 값 |
|---|---|
| 서비스 이름 | `payment-service` |
| 내부 실행 포트 | `8082` |
| Docker 노출 포트 | `8082:8082` |
| Docker 이미지 실행 프로필 | `prod` |
| Eureka | `${EUREKA_DEFAULT_ZONE}` 기본값 `http://localhost:8761/eureka` |
| Config Server | `${CONFIG_SERVER_URL}` 기본값 `http://localhost:8888` |
| DB | PostgreSQL `goods_mall`, 기본 스키마 `payment` |
| Swagger Docs | `/v3/api-docs` |
| Swagger UI | `/swagger-ui.html` |

직접 호출 기준 기본 주소:

```text
http://localhost:8082
```

Gateway 경유 기준 기본 주소:

```text
http://localhost:8080
```

## 3. Docker 기준 실행

### 3.1 컨테이너 빌드/실행 방식

`payment/Dockerfile`은 멀티 스테이지 빌드로 구성되어 있습니다.

- 빌드 스테이지: `eclipse-temurin:21-jdk`
- 런타임 스테이지: `eclipse-temurin:21-jre`
- 빌드 산출물: `payment-0.0.1-SNAPSHOT.jar`
- 컨테이너 시작 명령: `java -Dspring.profiles.active=prod -jar /app/app.jar`
- 컨테이너 노출 포트: `8082`

즉, Docker/AWS 환경에서는 기본적으로 `prod` 프로필로 동작한다고 보면 됩니다.

### 3.2 docker-compose 기준 payment 설정

현재 `docker-compose.yml` 기준 `payment` 컨테이너는 아래 값으로 실행됩니다.

- 컨테이너 이름: `goods-mall-payment`
- 포트 매핑: `8082:8082`
- `env_file`: `.env`
- `CONFIG_SERVER_URL`: `http://config:8888`
- `EUREKA_INSTANCE_HOSTNAME`: `payment`
- `EUREKA_INSTANCE_PREFER_IP_ADDRESS`: `false`

컨테이너 내부 통신 기준 의존 서비스:

- Config Server: `http://config:8888`
- Eureka: `${EUREKA_DEFAULT_ZONE}`
- Kafka: `${SPRING_KAFKA_BOOTSTRAP_SERVERS}`
- PostgreSQL: `${DB_URL}`

### 3.3 Docker에서 필요한 환경변수

최소한 아래 값들이 주입되어야 합니다.

| 분류 | 환경변수 |
|---|---|
| DB | `DB_URL`, `DB_USER_NAME`, `DB_USER_PASSWORD` |
| Kafka | `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| Eureka | `EUREKA_DEFAULT_ZONE` |
| Toss | `TOSS_PAYMENTS_BASE_URL`, `TOSS_PAYMENTS_CLIENT_KEY`, `TOSS_PAYMENTS_SECRET_KEY`, `TOSS_PAYMENTS_SUCCESS_URL`, `TOSS_PAYMENTS_FAIL_URL`, `TOSS_PAYMENTS_WIDGET_ENABLED` |
| Payment Kafka | `PAYMENT_KAFKA_TOPIC_*`, `PAYMENT_KAFKA_CONSUMER_GROUP_*`, `PAYMENT_KAFKA_RETRY_*` |
로컬 Docker Compose 예시 성격의 값은 루트 `.env.example`과 `payment/.env`에 포함되어 있습니다.

## 4. AWS 배포 고려사항

### 4.1 AWS에서 반드시 분리해야 하는 값

`.env.aws.example` 기준으로 아래 값들은 Git에 두지 말고 런타임 주입이 필요합니다.

- `DB_URL`
- `DB_USER_NAME`
- `DB_USER_PASSWORD`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET_KEY`
- `TOSS_PAYMENTS_CLIENT_KEY`
- `TOSS_PAYMENTS_SECRET_KEY`
- `TOSS_PAYMENTS_SUCCESS_URL`
- `TOSS_PAYMENTS_FAIL_URL`
- `EUREKA_DEFAULT_ZONE`

권장 저장 위치:

- AWS SSM Parameter Store
- AWS Secrets Manager
- ECS task definition secret injection 또는 EC2 환경변수 주입

### 4.2 AWS 네트워크 관점

이 서비스는 외부/내부로 아래 연결이 필요합니다.

- PostgreSQL에 TCP 연결 가능해야 함
- Kafka 또는 MSK bootstrap 서버에 연결 가능해야 함
- Eureka 서버에 등록 가능해야 함
- Config Server에 접근 가능해야 함
- Toss Payments 외부 API(`https://api.tosspayments.com`) 아웃바운드 호출 가능해야 함

AWS에서 권장되는 형태:

- 애플리케이션 간 통신은 Private Subnet 또는 내부 DNS 사용
- DB URL은 Private IP 또는 내부 DNS 사용
- Eureka / Config / Kafka도 가능하면 내부 주소 사용
- Toss 연동을 위해 NAT Gateway 또는 적절한 egress 경로 필요

### 4.3 AWS 배포 시 URL/도메인 주의점

Toss 결제 완료 후 프론트엔드로 돌아가는 URL은 환경별로 달라져야 합니다.

- `TOSS_PAYMENTS_SUCCESS_URL`
- `TOSS_PAYMENTS_FAIL_URL`

운영에서는 반드시 실제 프론트엔드 도메인 기준 HTTPS URL을 사용해야 합니다.

예시:

```text
https://<frontend-domain>/payments/toss/success
https://<frontend-domain>/payments/toss/fail
```

### 4.4 AWS에서 자주 확인할 항목

- `server.port=8082` 로 서비스 포트가 맞는지
- ALB 또는 Target Group이 `8082`로 연결되는지
- Eureka 등록 hostname/ip 설정이 현재 배포 방식과 맞는지
- Kafka bootstrap 주소가 VPC 내부에서 실제 해석되는지
- Config Server 장애 시 `fail-fast: true` 때문에 기동이 실패할 수 있는지
## 5. 기술 스택과 외부 의존성

- Java 21
- Spring Boot 4.0.3
- Spring Web, Validation, Data JPA
- PostgreSQL
- Kafka
- Eureka Client
- Spring Cloud Config
- Springdoc OpenAPI
- Toss Payments API

## 6. 도메인 개념

| 개념 | 설명 |
|---|---|
| `Wallet` | 회원별 잔액 계좌 |
| `Charge` | 충전 요청 및 승인 상태 |
| `ChargeRefund` | 충전 환불 이력 |
| `WalletTransaction` | 충전/환불/주문결제/정산금 적립 내역 |
| `Escrow` | 주문 결제 후 판매자별로 잠시 보관되는 금액 |

## 7. 모듈 간 통신 구조

### 7.1 HTTP로 받는 요청

| Method | Path | 인증 | 목적            |
|---|---|---|---------------|
| `GET` | `/api/payments/wallet` | 필요 | 내 지갑 금액 조회    |
| `GET` | `/api/payments/charges` | 필요 | 내 충전 목록 조회    |
| `GET` | `/api/payments/charges/{chargeId}` | 필요 | 충전 상세 조회      |
| `GET` | `/api/payments/refunds` | 필요 | 내 환불 목록 조회    |
| `GET` | `/api/payments/transactions` | 필요 | 내 지갑 거래 내역 조회 |
| `GET` | `/api/payments/seller/pending-incomes` | 필요 | 판매자 미정산 수입 조회 |
| `POST` | `/api/payments/charge` | 필요 | 충전 요청 생성      |
| `POST` | `/api/payments/confirm` | 없음 | Toss 승인 결과 반영 |
| `POST` | `/api/payments/charges/{chargeId}/refund` | 현재 없음 | 충전 환불         |
| `POST` | `/api/payments/orders` | 없음 | 주문 결제 API     |

인증이 필요한 API는 `gateway`를 거치면서 JWT 기반으로 `@CurrentMember`가 주입됩니다.

현재 코드 기준 주의사항:

- `POST /api/payments/confirm`은 PG 승인 콜백 성격이라 회원 인증 없이 동작합니다.
- `POST /api/payments/orders`는 내부 서비스 간 호출용 API입니다.
- `POST /api/payments/charges/{chargeId}/refund`는 PG 환불 API로, 현재는 엔드 포인트만 존재하며 구현이 완료되지 않았습니다.

### 7.2 Kafka로 받는 이벤트

| Topic | 메시지 타입 | 목적 |
|---|---|---|
| `member-signed-up` | `MemberCreatedMessage` | 회원 생성 시 wallet 생성 |
| `order.purchase-confirmed` | `OrderPurchaseConfirmedMessage` | 수동 구매 확정 시 escrow release |
| `settlement.seller-payout-requested` | `SellerSettlementPayoutRequestedMessage` | 정산 지급 요청 처리 |

- 결제 관련해서는 이벤트를 잘 사용하지 않는다고 하여 order와 API 통신으로 변경될 수 있습니다.
- 구매 확정 책임을 order가 가지고 갈 예정이므로 리스너 이벤트는 변경 가능성 있습니다.

### 7.3 Kafka로 발행하는 이벤트

| Topic | 메시지 타입 | 목적 |
|---|---|---|
| `payment.order-payment-result` | `OrderPaymentResultMessage` | 주문 결제 결과를 order 쪽에 전달 |
| `payment.settlement-candidate-created` | `SettlementCandidateCreatedMessage` | 정산 후보 생성 알림 |
| `payment.seller-payout-result` | `SellerSettlementPayoutResultMessage` | 정산금 지급 결과를 settlement에 전달 |

- 결제 관련해서는 이벤트를 잘 사용하지 않는다고 하여 order와 API 통신으로 변경될 수 있습니다.

## 8. 공통 응답 형식

모든 HTTP 응답은 `ApiResponse<T>` 포맷을 사용합니다.

성공:

```json
{
  "success": true,
  "data": {
    "...": "..."
  },
  "error": null
}
```

실패:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_INPUT_VALUE",
    "message": "amount must be positive."
  }
}
```

### 자주 쓰는 에러 코드

| 코드 | HTTP Status | 의미 |
|---|---|---|
| `INVALID_TOKEN` | `401` | 인증 토큰 문제 |
| `INVALID_CHARGE_REQUEST` | `400` | 충전 요청 값 오류 |
| `INVALID_ORDER_PAYMENT_REQUEST` | `400` | 주문 결제 요청 값 오류 |
| `INVALID_INPUT_VALUE` | `400` | 페이지 번호, 크기, 필수값 등 입력 오류 |
| `CHARGE_NOT_FOUND` | `404` | 충전 내역 없음 |
| `WALLET_NOT_FOUND` | `404` | 지갑 없음 |
| `ESCROW_NOT_FOUND` | `404` | 에스크로 없음 |
| `INVALID_STATE` | `409` | 현재 상태에서 처리 불가 |
| `PAYMENT_GATEWAY_ERROR` | `502` | Toss 연동 실패 |

## 9. HTTP API 상세

### 9.1 조회 API

#### `GET /api/payments/wallet`

내 지갑 요약을 조회합니다.

- 인증: 필요
- 요청값: 없음
- 응답 데이터 필드:
  - `walletId`
  - `memberId`
  - `balance`
  - `updatedAt`

#### `GET /api/payments/charges?page=0&size=20`

내 충전 목록을 최신 `requestedAt` 기준 내림차순으로 조회합니다.

- 인증: 필요
- 쿼리 파라미터:
  - `page`: 0 이상
  - `size`: 1 이상 100 이하
- 응답 데이터:
  - `items[]`
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`
  - `hasNext`

`items[]` 각 요소 필드:

- `chargeId`
- `requestedAmount`
- `approvedAmount`
- `chargeStatus`
- `pgProvider`
- `requestedAt`
- `approvedAt`
- `failedAt`

#### `GET /api/payments/charges/{chargeId}`

충전 상세를 조회합니다. 최신 환불 이력이 있으면 함께 내려줍니다.

- 인증: 필요
- 경로 변수:
  - `chargeId`
- 응답 데이터 주요 필드:
  - `chargeId`
  - `memberId`
  - `walletId`
  - `requestedAmount`
  - `approvedAmount`
  - `pgProvider`
  - `pgOrderId`
  - `pgPaymentKey`
  - `chargeStatus`
  - `requestedAt`
  - `approvedAt`
  - `failedAt`
  - `failureReason`
  - `hasRefundHistory`
  - `latestRefund`

#### `GET /api/payments/refunds?page=0&size=20`

내 환불 목록을 조회합니다.

- 인증: 필요
- 쿼리 파라미터:
  - `page`: 0 이상
  - `size`: 1 이상 100 이하
- `items[]` 각 요소 필드:
  - `chargeRefundId`
  - `chargeId`
  - `refundAmount`
  - `refundStatus`
  - `refundReason`
  - `requestedAt`
  - `refundedAt`
  - `failedAt`

#### `GET /api/payments/transactions?page=0&size=20`

지갑 거래 내역을 조회합니다. 충전, 환불, 주문 결제, 정산금 지급이 모두 포함됩니다.

- 인증: 필요
- 쿼리 파라미터:
  - `page`: 0 이상
  - `size`: 1 이상 100 이하
- `items[]` 각 요소 필드:
  - `transactionId`
  - `transactionType`
  - `amount`
  - `balanceAfter`
  - `referenceType`
  - `referenceId`
  - `description`
  - `createdAt`

#### `GET /api/payments/seller/pending-incomes?page=0&size=20`

판매자 기준 아직 wallet에 반영되지 않은 `HELD escrow`를 조회합니다.

- 인증: 필요
- 쿼리 파라미터:
  - `page`: 0 이상
  - `size`: 1 이상 100 이하
- `items[]` 각 요소 필드:
  - `escrowId`
  - `orderId`
  - `amount`
  - `escrowStatus`
  - `releaseAt`
  - `createdAt`
  - `updatedAt`

### 9.2 충전/환불 API

#### `POST /api/payments/charge`

충전 요청을 생성합니다. 이 단계에서는 실제 잔액이 증가하지 않고, PG 승인 전의 `PENDING charge`만 만듭니다.

- 인증: 필요
- 요청 본문:

```json
{
  "amount": 10000
}
```

- 필수값:
  - `amount`: 양수

- 응답 데이터 필드:
  - `chargeId`
  - `walletId`
  - `pgOrderId`
  - `amount`
  - `chargeStatus`

#### `POST /api/payments/confirm`

Toss 승인 결과를 반영합니다. 승인 성공 시 charge 상태를 확정하고 wallet 잔액을 증가시킵니다.

- 인증: 없음
- 요청 본문:

```json
{
  "chargeId": "UUID",
  "paymentKey": "toss payment key",
  "orderId": "CHARGE-...",
  "amount": 10000
}
```

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

#### `POST /api/payments/charges/{chargeId}/refund`

승인 완료된 charge를 환불합니다. PG 취소 성공 시 wallet 잔액을 차감하고 환불 이력을 남깁니다.

- 인증: 현재 없음
- 경로 변수:
  - `chargeId`
- 요청 본문:

```json
{
  "refundReason": "단순 환불"
}
```

- 필수값:
  - `refundReason`

- 응답 데이터 필드:
  - `chargeId`
  - `refundStatus`
  - `refundedAmount`
  - `walletBalance`
  - `refundedAt`

### 9.3 주문 결제 API

#### `POST /api/payments/orders`

주문 서비스가 호출하는 내부 API입니다. 구매자 지갑 차감 후 판매자별 `escrow`를 생성하고, 결과는 HTTP 응답과 Kafka 이벤트로 함께 전달합니다.

- 인증: 없음
- 요청 본문:

```json
{
  "orderId": "UUID",
  "buyerId": "UUID",
  "totalPrice": 12000,
  "requestedAt": "2026-04-10T09:00:00Z",
  "orderLines": [
    {
      "orderItemId": "UUID",
      "sellerId": "UUID",
      "unitPriceSnapshot": 6000,
      "quantity": 2,
      "lineTotalPrice": 12000
    }
  ]
}
```

- 필수값:
  - `orderId`
  - `buyerId`
  - `totalPrice`
  - `requestedAt`
  - `orderLines`
  - 각 `orderLine`의 `orderItemId`, `sellerId`, `unitPriceSnapshot`, `quantity`, `lineTotalPrice`

- 검증 규칙:
  - `orderLines`는 비어 있으면 안 된다.
  - 각 금액은 양수여야 한다.
  - `sum(orderLines.lineTotalPrice) == totalPrice` 이어야 한다.
  - `BigDecimal` 금액은 소수 없는 정수 금액이어야 한다.

- 응답 데이터 필드:
  - `orderId`
  - `buyerMemberId`
  - `amount`
  - `status`: `SUCCESS` or `FAILED`
  - `reasonCode`: 실패 시 원인 코드

## 10. 주문 결제와 정산 흐름

### 10.1 회원 생성 시

1. member 모듈이 `member-signed-up` 발행
2. payment가 이벤트를 받아 wallet 생성

### 10.2 충전 시

1. 클라이언트가 `POST /api/payments/charge` 호출
2. payment가 `PENDING charge` 생성
3. 클라이언트가 Toss 결제 완료 후 `POST /api/payments/confirm` 호출
4. payment가 Toss 승인 확인 후 wallet 잔액 증가

### 10.3 주문 결제 시

1. order 모듈이 `POST /api/payments/orders` 호출
2. payment가 구매자 wallet 잔액 차감
3. seller별 `escrow` 생성
   - 부분 취소및 부분 환불로 인하여 seller별이 아닌 order_item 별로 변경될 예정입니다.
4. payment가 `payment.order-payment-result` 이벤트 발행

### 10.4 구매 확정 시

1. order 모듈이 `order.purchase-confirmed` 발행
2. payment가 escrow release
3. `payment.settlement-candidate-created` 이벤트 발행

### 10.5 판매자 환불 시

1. 판매자가 환불 요청 API를 호출
2. payment가 `HELD escrow` 상태인지 검증
3. 환불 금액 계산 후 환불 이력과 `escrow_transaction(REFUND)` 기록
4. 왕복 배송비 `6000원` 차감 정책을 반영
5. 구매 확정 이후 상태면 환불 불가 처리

### 10.6 정산 지급 시

1. settlement 모듈이 `settlement.seller-payout-requested` 발행
2. payment가 판매자 wallet에 정산금 적립
3. 결과를 `payment.seller-payout-result`로 회신

## 11. Kafka 메시지 핵심 필드

### payment가 소비하는 메시지

| 메시지 | 주요 필드 |
|---|---|
| `MemberCreatedMessage` | `eventId`, `memberId`, `email`, `occurredAt` |
| `OrderPurchaseConfirmedMessage` | `eventId`, `orderId`, `sellerMemberId`, `confirmedAt`, `confirmationType` |
| `SellerSettlementPayoutRequestedMessage` | `eventId`, `settlementId`, `sellerMemberId`, `settlementYear`, `settlementMonth`, `payoutAmount`, `requestedAt` |

### payment가 발행하는 메시지

| 메시지 | 주요 필드 |
|---|---|
| `OrderPaymentResultMessage` | `eventId`, `orderId`, `buyerMemberId`, `amount`, `status`, `reasonCode`, `occurredAt` |
| `SettlementCandidateCreatedMessage` | `eventId`, `orderId`, `escrowId`, `sellerMemberId`, `grossAmount`, `releasedAt`, `confirmationType`, `occurredAt` |
| `SellerSettlementPayoutResultMessage` | `eventId`, `requestEventId`, `settlementId`, `sellerMemberId`, `payoutAmount`, `resultStatus`, `failureReason`, `processedAt` |

## 12. 운영 시 참고사항

- `payment`는 `Toss Payments` 승인/취소를 호출하므로 `TOSS_PAYMENTS_*` 환경변수가 필요합니다.
- 지갑/결제/에스크로 데이터는 `payment` 스키마를 사용합니다.
- Swagger 문서는 gateway 기준 Bearer Token 입력을 전제로 구성되어 있습니다.
- 페이지 조회 API는 모두 `size <= 100` 제한이 있습니다.
- settlement 지급 요청은 중복 처리 방지를 위해 `referenceId = settlementId + referenceType = "SETTLEMENT`을 확인합니다.
- Docker 실행 시 기본 프로필은 `prod`입니다.
- AWS 운영에서는 `.env.aws.example` 값을 기준으로 시크릿/환경변수를 분리 주입하는 전제가 필요합니다.

## 13. 현재 구현상 메모

- `refund` API에는 소유자 검증 TODO가 남아 있습니다.
- 주문 결제 API는 HTTP 응답과 별도로 Kafka 결과 이벤트도 발행합니다.
- 구매 확정 이후 환불은 현재 정책상 허용하지 않습니다.
