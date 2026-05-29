# Event Strategy

## Table of Contents

- [1. Overview](#1-overview)
- [2. Event Model](#2-event-model)
  - [2.1 Event Contract](#21-event-contract)
  - [2.2 Event Naming Convention](#22-event-naming-convention)
    - [2.2.1 Kafka Topic Name](#221-kafka-topic-name)
    - [2.2.2 Event Envelope Type](#222-event-envelope-type)
- [3. Delivery and Consistency](#3-delivery-and-consistency)
  - [3.1 Consistency Strategy](#31-consistency-strategy)
  - [3.2 Idempotency](#32-idempotency)
  - [3.3 Retry / DLQ Strategy](#33-retry--dlq-strategy)
  - [3.4 Ordering Strategy](#34-ordering-strategy)
  - [3.5 Outbox Pattern](#35-outbox-pattern)
- [4. Failure Scenarios](#4-failure-scenarios)
- [5. Related Docs](#5-related-docs)

---

## 1. Overview

이 문서는 서비스별 이벤트 목록이 아니라, 프로젝트 전반의 이벤트 설계 원칙과 운영 전략을 설명한다.

서비스별 producer/consumer 이벤트는 [service](service/README.md) 하위 문서를 참고한다.

---

## 2. Event Model

### 2.1 Event Contract

이벤트는 가능한 경우 공통 envelope와 이벤트별 payload로 구성한다.

다만 현재 프로젝트에는 레거시 또는 단순 이벤트 흐름도 함께 존재한다. 따라서 모든 Kafka 메시지가 `EventEnvelope`를 사용하는 것은 아니다.

다음 규칙은 신규 구현과 기존 코드 수정에서 기본 계약으로 유지한다. [Required]

- `EventEnvelope`를 사용하는 이벤트는 아래 공통 필드 의미를 유지해야 한다.
- `eventId`, `eventType`, `occurredAt`, `payload`는 이벤트 식별과 처리에 필요한 핵심 필드다.

```json
{
  "eventId": "UUID",
  "eventType": "payment.bid-fee.charge.succeeded",
  "source": "payment-service",
  "aggregateId": "UUID",
  "recipientId": "UUID",
  "occurredAt": "2026-05-26T10:15:30+09:00",
  "traceId": "UUID or trace-id",
  "payload": {}
}
```

### 2.1.1 공통 필드

| Field | Description |
|---|---|
| `eventId` | 이벤트 중복 처리를 위한 고유 ID |
| `eventType` | 이벤트 종류 |
| `source` | 이벤트를 발행한 서비스 |
| `aggregateId` | 이벤트가 발생한 핵심 도메인 ID |
| `recipientId` | 이벤트 수신 대상 사용자 또는 서비스 처리 기준 ID |
| `occurredAt` | 이벤트 발생 시각 |
| `traceId` | 추적/로그 상관관계를 위한 ID |
| `payload` | 이벤트별 상세 데이터 |

가능하면 다음 기준을 함께 따른다. [Recommended]

- 신규 서비스 간 이벤트는 `EventEnvelope` 사용을 기본으로 한다.
- `source`, `aggregateId`, `recipientId`, `traceId`는 가능한 경우 채운다.

현재 프로젝트에는 다음 예외 구현이 존재한다. [Exception]

- raw payload 사용: cart 이벤트, product 일부 이벤트
- 기존 raw payload 이벤트는 소비자 호환성을 고려해 별도 마이그레이션 계획 없이 즉시 변경하지 않는다.

### 2.1.2 현재 적용 상태

- `EventEnvelope` 사용: member, order, payment, settlement, notification 일부 이벤트
- raw payload 사용: cart 이벤트, product 일부 이벤트

---

### 2.2 Event Naming Convention

이 프로젝트에서 이벤트 이름은 다음 두 위치에 사용된다.

- Kafka topic name
- Event envelope의 `eventType`

두 값은 역할이 다르므로 컨벤션을 분리해서 관리한다.

### 2.2.1 Kafka Topic Name

Kafka topic name은 서비스 간 라우팅 단위다.

다음 규칙은 topic 계약으로 반드시 유지해야 한다. [Required]

- topic name은 consumer와 producer가 공유하는 계약이므로, 기존 topic 변경 시 consumer 호환성을 검토해야 한다.

신규 topic은 가능하면 다음 형식을 우선 사용한다. [Recommended]

소문자, dot(`.`), kebab-case(`-`)를 사용한다.

```text
도메인.상태변화
도메인.행위.결과
```

좋은 예:

```text
auction.won
order.purchase-confirmed
payment.bid-fee.charge.succeeded
payment.settlement-candidate-created
```

신규 topic은 `도메인.상태변화` 또는 `도메인.행위.결과` 형식을 우선 사용한다.

현재 프로젝트에는 다음 예외 topic 형식이 존재한다. [Exception]

현재 프로젝트에는 dot 없이 kebab-case만 사용하는 topic도 존재한다.

```text
member-signed-up
member-seller-promoted
member-account-verification-expired
member-oauth-linked
```

기존 topic은 consumer 호환성을 위해 유지한다.

피해야 할 예:

```text
createSettlement
sendNotification
confirmPayment
```

### 2.2.2 Event Envelope Type

`EventEnvelope.eventType`은 envelope 내부에서 이벤트 종류를 식별하는 값이다.

다음 규칙은 envelope 내부 식별 값으로 반드시 유지해야 한다. [Required]

- `eventType`은 topic name과 의미적으로 같은 이벤트를 가리켜야 한다.
- consumer가 특정 `eventType`을 검증하는 경우 producer와 consumer의 값이 반드시 일치해야 한다.

현재 구현과 신규 추가 시에는 다음 형식을 우선 사용한다. [Recommended]

현재 프로젝트 구현은 `UPPER_SNAKE_CASE`를 사용한다.

```text
상태변화
도메인_상태변화
도메인_행위_결과
```

좋은 예:

```text
AUCTION_WON
ORDER_PURCHASE_CONFIRMED
BID_FEE_CHARGE_SUCCEEDED
SETTLEMENT_CANDIDATE_CREATED
SELLER_SETTLEMENT_PAYOUT_REQUESTED
```

예:

| Kafka topic name | EventEnvelope.eventType |
|---|---|
| `auction.won` | `AUCTION_WON` |
| `order.purchase-confirmed` | `ORDER_PURCHASE_CONFIRMED` |
| `payment.bid-fee.charge.succeeded` | `BID_FEE_CHARGE_SUCCEEDED` |
| `payment.settlement-candidate-created` | `SETTLEMENT_CANDIDATE_CREATED` |

---

## 3. Delivery and Consistency

### 3.1 Consistency Strategy

### 3.1.1 Eventual Consistency

이 시스템은 모든 서비스 상태가 즉시 동일해지는 구조가 아니다.

다음 규칙은 서비스 간 상태 전파에서 반드시 지켜야 한다. [Required]

- 이벤트를 수신한 서비스는 자신이 소유한 상태만 변경해야 한다.
- 다른 서비스의 원본 도메인 상태를 직접 수정하지 않는다.

가능하면 다음 방식으로 상태 전파를 설계한다. [Recommended]

각 서비스는 자신의 상태를 먼저 저장하고, 이벤트를 통해 다른 서비스가 후속 상태를 갱신한다. 따라서 서비스 간 상태는 짧은 시간 동안 다를 수 있으며, 최종적으로 일관된 상태에 도달한다.

현재 프로젝트는 order, payment, settlement, auction, notification 등 주요 흐름에서 이벤트 기반 후속 처리를 사용한다. 다만 모든 서비스 간 연동이 동일한 수준의 이벤트 전략으로 통일되어 있지는 않다.

예:

```text
Auction: PENDING_PAYMENT
Order: PENDING_PAYMENT
Payment: 결제 대기

이후 order.confirmed 이벤트 수신

Auction: COMPLETED
Order: CONFIRMED
Payment: PAID
```

---

### 3.2 Idempotency

Kafka는 동일 이벤트가 중복 전달될 수 있으므로 Consumer는 멱등하게 동작해야 한다.

다음 규칙은 consumer 구현에서 반드시 포함해야 한다. [Required]

- consumer는 동일 이벤트가 중복 전달되어도 비즈니스 상태를 중복 변경하지 않아야 한다.
- 멱등 기준은 `eventId`, 비즈니스 키, 상태 전이 조건 중 하나 이상으로 명확해야 한다.

신규 consumer는 가능하면 다음 방식 중 하나 이상을 사용한다. [Recommended]

- `eventId` 기반 처리 이력 저장
- 이미 처리한 이벤트는 skip
- 비즈니스 키 기반 중복 방지
- 상태 전이 조건 검증
- 중복 성공 이벤트는 no-op 처리

신규 consumer는 `eventId` 기반 또는 명확한 비즈니스 키 기반 멱등 처리를 포함해야 한다.

현재 프로젝트에는 다음 예외 구현이 존재한다. [Exception]

그 외 consumer는 별도 처리 이력 저장소가 없거나 상태 전이 검증에 의존한다.

### 3.2.1 현재 적용 상태

현재 프로젝트는 모든 consumer가 동일한 방식의 `eventId` 처리 이력 저장소를 사용하지 않는다.

- AI product event: Redis 기반 idempotency key 사용
- Notification: `eventId + memberId + type` 기준 중복 알림 생성 방지
- Settlement: `escrowId`, settlement 상태, item 상태 등 비즈니스 키/상태 기반 중복 방지
- Auction: 입찰/경매 상태 전이 조건으로 중복 이벤트 일부 no-op 처리
- 그 외 consumer: 별도 처리 이력 저장소가 없거나 상태 전이 검증에 의존한다.

예:

```text
order.confirmed 이벤트가 중복 수신되어도
Order 상태가 이미 CONFIRMED라면 다시 처리하지 않는다.
```

---

### 3.3 Retry / DLQ Strategy

### 3.3.1 Retry

일시적 장애가 발생하면 Consumer는 재시도한다.

다음 규칙은 consumer 실패 처리에서 반드시 명확해야 한다. [Required]

- consumer 실패 처리 방식은 명확해야 한다.
- 재시도 가능한 예외와 즉시 실패 처리할 예외를 구분해야 한다.

일시적 장애로 간주할 수 있는 예시는 다음과 같다. [Recommended]

예:

- DB connection 일시 장애
- 외부 API timeout
- Kafka consumer 일시 실패
- 일시적인 lock 경합

### 3.3.2 DLQ

재시도 후에도 실패하면 DLQ로 이동시켜 운영자가 확인하거나 별도 재처리한다.

```text
main topic
→ retry topic
→ DLQ
```

DLQ로 이동한 이벤트는 원인 확인 후 재처리 가능해야 한다.

신규 consumer는 실패 시 재시도 정책과 DLQ 처리 방식을 명시해야 한다.

현재 프로젝트에는 다음 예외 구현이 존재한다. [Exception]

현재 프로젝트에는 Retry / DLQ가 적용되지 않았거나 Spring Kafka 기본 처리에 의존하는 consumer가 존재한다.

### 3.3.3 현재 적용 상태

Retry / DLQ는 일부 consumer에만 적용되어 있다.

- 적용: AI product event, notification event, payment 일부 consumer, settlement consumer
- 미적용 또는 기본 처리 의존: order consumer, product consumer, auction 일부 consumer

---

### 3.4 Ordering Strategy

이벤트 순서가 중요한 경우 partition key를 명확히 설정한다.

다음 규칙은 순서 의존 이벤트에서 반드시 정해야 한다. [Required]

- 순서가 중요한 이벤트는 partition key를 명확히 정해야 한다.
- 서로 다른 aggregate 간 전체 순서는 보장하지 않는다.

신규 이벤트는 가능하면 다음 기준으로 partition key를 정한다. [Recommended]

| Flow | Partition Key |
|---|---|
| Auction bid events | `auctionId` |
| Bid fee events | `bidId` |
| Order events | `orderId` |
| Payment events | `paymentId` |
| Settlement events | `settlementId` |
| Member events | `memberId` |

현재 프로젝트는 outbox 또는 producer 구현에서 `aggregateId`, `orderId`, `bidId`, `auctionId`, `memberId`, `settlementId` 등을 partition key로 사용한다. 다만 일부 direct publisher는 이벤트별로 key 기준이 다르므로, 신규 이벤트 추가 시 partition key를 명시적으로 결정해야 한다.

---

### 3.5 Outbox Pattern

DB 상태 변경과 이벤트 발행 사이의 불일치를 줄이기 위해 Outbox 패턴을 사용한다.

현재 프로젝트에서 Outbox 패턴은 모든 producer에 일괄 적용되어 있지는 않다.

다음 규칙은 outbox 흐름에서 반드시 지켜야 한다. [Required]

- outbox 상태는 Kafka 발행 성공 이후 `PUBLISHED`로 변경한다.
- 발행 실패 이벤트는 재발행 가능해야 한다.

신규 발행 흐름은 가능하면 outbox를 우선 사용한다. [Recommended]

- DB 상태 변경과 함께 발행되는 신규 이벤트는 Outbox 사용을 우선한다.

### 3.5.1 문제

```text
DB 저장 성공
Kafka 발행 실패
```

또는

```text
Kafka 발행 성공
DB transaction rollback
```

이 발생하면 서비스 간 상태가 어긋날 수 있다.

### 3.5.2 해결 방향

```text
1. 비즈니스 상태 변경
2. Outbox 테이블에 이벤트 저장
3. Transaction commit
4. 별도 publisher가 outbox 이벤트 발행
5. 발행 완료 처리
```

현재 프로젝트에는 다음 예외 구현이 존재한다. [Exception]

- direct publish 흐름: cart, member, auction 일부 publisher, payment 일부 publisher
- 일부 기존 outbox 구현은 발행 성공 확인 전에 `PUBLISHED`로 변경될 수 있다.

### 3.5.3 현재 적용 상태

- 적용: order, product, payment outbox 흐름, settlement payout 요청 흐름, auction 일부 흐름
- 미적용 또는 direct publish: cart, member, auction 일부 publisher, payment 일부 publisher

단순 알림성 이벤트나 기존 direct publish 흐름은 현재 구현을 유지하되, 장애 시 유실 가능성을 별도로 감수한다.

주의: outbox 상태는 Kafka 발행 성공 이후 `PUBLISHED`로 변경하는 것이 원칙이다. 기존 구현 중 일부는 이 원칙과 다를 수 있으므로, 수정 시 발행 성공 확인 후 상태를 변경하도록 정비한다.

---

## 4. Failure Scenarios

| Scenario | Risk | Handling |
|---|---|---|
| 이벤트 중복 수신 | 중복 상태 변경 | idempotency |
| 이벤트 처리 실패 | 후속 처리 누락 | retry / DLQ |
| 오래된 이벤트 도착 | 최신 상태 덮어쓰기 | 상태 전이 조건 또는 version check |
| Kafka 장애 | 이벤트 지연 | outbox + 재발행 |
| Consumer 장애 | 처리 지연 | consumer group / retry |
| DB 저장 성공 후 이벤트 발행 실패 | 서비스 간 상태 불일치 | outbox relay 재발행 |
| 지급 결과 이벤트 중복 수신 | 중복 완료 또는 중복 실패 처리 | 비즈니스 키 기준 멱등 처리 |

---

## 5. Related Docs

- [User Flow](01-user-flow.md)
- [Service Responsibilities](03-service-responsibilities.md)
- [Service Docs](service/README.md)
- [Settlement Flow](service/07-settlement-flow.md)
- [Kafka Strategy](tech/kafka.md)
- [Troubleshooting](09-troubleshooting.md)
- [Engineering Decisions](10-engineering-decisions.md)
