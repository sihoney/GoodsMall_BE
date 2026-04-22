# 공통 이벤트 Envelope 정의

## 목적
`notification` 모듈이 Kafka로 들어오는 여러 이벤트를 같은 방식으로 처리할 수 있도록, 공통 envelope 규격을 정의한다.

이 문서는 다음을 위한 기준 문서이다.

- 이벤트 형식 표준화
- Notification consumer 공통화
- 중복 처리와 멱등성 확보
- 로그 추적과 장애 분석

## 왜 필요한가
현재 백엔드에서는 모듈별로 이벤트와 contract의 필드 구성이 조금씩 다르다.

예를 들면:

- 어떤 이벤트는 `eventType`을 가진다.
- 어떤 이벤트는 `occurredAt`만 가진다.
- 어떤 contract는 `reasonCode`를 가지고, 어떤 것은 가지고 있지 않다.

이 상태에서는 Notification consumer가 이벤트마다 다른 해석 로직을 가져야 하므로, 공통 처리와 운영 안정성이 떨어진다.

## 공통 Envelope 예시

```java
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String source,
        UUID aggregateId,
        UUID recipientId,
        Instant occurredAt,
        String traceId,
        T payload
) {
}
```

## 필드 설명

### `eventId`
- 이벤트의 고유 식별자
- 중복 처리의 기준값으로 사용

### `eventType`
- 이벤트 종류를 나타내는 값
- 예: `ORDER_PAYMENT_RESULT`, `AUTO_PURCHASE_CONFIRMED`

### `source`
- 이벤트를 발행한 서비스 이름
- 예: `member-service`, `payment-service`

### `aggregateId`
- 이벤트의 기준이 되는 도메인 식별자
- 예: 주문 ID, 회원 ID, 정산 ID

### `recipientId`
- 알림을 받아야 하는 대상 식별자
- Notification 저장 및 SSE push 대상 결정에 사용

### `occurredAt`
- 이벤트가 실제로 발생한 시각
- 로그, 알림 생성 시각, 정렬 기준에 사용

### `traceId`
- 요청 흐름을 추적하기 위한 식별자
- gateway에서 생성하거나 전달받아 사용

### `payload`
- 도메인별 실제 데이터
- 공통 메타 정보가 아닌 비즈니스 내용을 담는다

## payload 예시

### 회원 가입 이벤트

```java
public record MemberSignedUpPayload(
        UUID memberId,
        String email
) {
}
```

### 주문 생성 이벤트

```java
public record OrderCreatedPayload(
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice
) {
}
```

### 결제 결과 이벤트

```java
public record OrderPaymentResultPayload(
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal amount,
        String status,
        String reasonCode
) {
}
```

## Notification consumer에서의 사용 방식

1. Kafka 메시지를 수신한다.
2. 공통 envelope로 역직렬화한다.
3. `eventType`을 읽는다.
4. `payload`를 해당 타입으로 변환한다.
5. Notification 저장 로직을 수행한다.
6. SSE로 실시간 푸시한다.

## 표준화 기준

다음 규칙을 권장한다.

- `eventId`는 UUID로 통일한다.
- `occurredAt`은 공통 발생 시각으로 사용한다.
- `traceId`는 gateway에서 생성한 값을 우선 사용한다.
- payload에는 비즈니스 데이터만 넣는다.
- `eventType`은 문자열 상수 또는 enum 이름으로 통일한다.

## 포함 범위

이 envelope 규격은 다음을 모두 포함한다.

- 내부 이벤트가 Kafka로 그대로 발행되는 경우
- 별도 contract DTO가 Kafka로 발행되는 경우

즉, 최종적으로 Kafka에 실려 가는 메시지라면 모두 이 규격을 따라야 한다.

## 제외 범위

다음은 이 문서의 직접 대상이 아니다.

- Notification DB 스키마 상세 설계
- SSE 연결 유지 전략
- 재시도와 DLQ 구현 상세
- Outbox 패턴

## 한 줄 요약
공통 이벤트 envelope는 Kafka로 들어오는 메시지의 바깥 형식을 하나로 맞추기 위한 기준이며, Notification consumer 공통화의 출발점이다.
