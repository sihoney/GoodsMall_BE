# Notification Event Design

## 현재 실제 지원 이벤트
현재 notification 모듈이 실제로 처리하는 이벤트는 아래 2개다.

| eventType | 구독 토픽 | handler | 생성 알림 |
| --- | --- | --- | --- |
| `MEMBER_SIGNED_UP` | `member-signed-up` | `MemberSignedUpNotificationEventHandler` | 회원 가입 완료 알림 |
| `ORDER_PAYMENT_RESULT` | `payment.order-payment-result` | `OrderPaymentResultNotificationEventHandler` | 주문 결제 성공/실패 알림 |

## 현재 미연결 이벤트
아래 이벤트는 enum과 서비스 메서드는 준비돼 있지만, 아직 실제 consumer handler로 연결되지 않았다.

| 알림 대상 | 상태 | 비고 |
| --- | --- | --- |
| `AUTO_PURCHASE_CONFIRMED` | 미연결 | 서비스 메서드만 존재 |
| `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED` | 미연결 | 서비스 메서드만 존재 |
| `SELLER_SETTLEMENT_PAYOUT_FAILED` | 미연결 | 서비스 메서드만 존재 |

## 현재 소비 구조
- 단일 Kafka listener가 여러 토픽을 구독한다.
- consumer는 공통 `EventEnvelope<JsonNode>`로 파싱한다.
- `eventType`을 기준으로 handler registry에서 handler를 선택한다.
- 각 handler가 payload 타입 변환, 검증, usecase 호출을 담당한다.

## Envelope-Only Consumer 흐름
1. Kafka listener가 raw message 수신
2. `EventEnvelope<JsonNode>` 파싱
3. `eventType` 추출
4. handler registry lookup
5. handler가 typed payload로 변환
6. validation 수행
7. `NotificationUsecase` 호출
8. 저장 후 `afterCommit` 기반 SSE push

## 이벤트별 처리 규칙

### MEMBER_SIGNED_UP
필수 필드:
- `eventId`
- `eventType = MEMBER_SIGNED_UP`
- `source`
- `recipientId`
- `occurredAt`
- `payload.memberId`
- `payload.email`

추가 규칙:
- `recipientId == payload.memberId`

처리 결과:
- `NotificationCommand` 생성
- `NotificationUsecase.createNotification(...)` 호출

### ORDER_PAYMENT_RESULT
필수 필드:
- `eventId`
- `eventType = ORDER_PAYMENT_RESULT`
- `source`
- `recipientId`
- `occurredAt`
- `traceId`
- `payload.orderId`
- `payload.buyerMemberId`
- `payload.amount`
- `payload.status`

추가 규칙:
- `payload.status == FAILED`이면 `payload.reasonCode` 필수
- `recipientId == payload.buyerMemberId`
- `payload.amount`는 whole number여야 함

처리 결과:
- `SUCCESS`면 `createOrderPaymentSucceededNotification(...)`
- `FAILED`면 `createOrderPaymentFailedNotification(...)`

## 실패 분기

### DLQ 대상
- envelope parse 실패
- 미지원 `eventType`
- 필수 필드 누락
- payload contract 위반
- payload 변환 실패

### RETRY 대상
- 일시적 런타임 오류
- 저장 계층 일시 오류

### IGNORE 대상
- 현재 consumer 계층에서 명시적으로 쓰고 있지는 않지만, 멱등성 중복 이벤트는 저장 단계에서 no-op 처리된다.

## 멱등성
- 저장 단계에서 `eventId` 기준 중복을 차단한다.
- 중복 이벤트는 예외를 발생시키지 않고 무시한다.

## UI 반영 규칙
- API payload에는 `referenceId`, `referenceType`이 포함될 수 있다.
- 현재 프론트 렌더링은 주로 아래 필드 중심이다.
  - `title`
  - `subtitle`
  - `content`
  - `actions`
  - `elapsedTime`

## 현재 merge 범위의 핵심 변화
이전:
- 이벤트별 listener 메서드
- consumer 내부 분기

현재:
- 단일 listener
- handler registry 기반 dispatch
- 공통 consumer failure classifier
- Kafka DLQ publisher

## 참고 문서
- [Architecture.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/Architecture.md)
- [CurrentConsumerFlowReview.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/CurrentConsumerFlowReview.md)
- [EnvelopeOnlyConsumerImplementationResult.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/EnvelopeOnlyConsumerImplementationResult.md)
- [DLQDiagram.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQDiagram.md)
