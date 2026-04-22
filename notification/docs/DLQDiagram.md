# Notification DLQ Diagram

## 목적
- notification 서비스에서 DLQ가 필요한 이유와 분기 기준을 시각적으로 설명한다.
- 현재 consumer 흐름 기준으로 어떤 실패가 `DLQ`, `RETRY`, `IGNORE`로 가는지 한눈에 보여준다.

## 1. 전체 개요
```mermaid
flowchart TD
    A[Kafka Message Received] --> B[Parse Event Envelope]
    B -->|Parse Success| C[Validate Envelope and Payload]
    B -->|Parse Failure| DLQ[Send to DLQ]

    C -->|Validation Success| D[Call Notification Service]
    C -->|Validation Failure| DLQ

    D -->|Duplicate Event| IGNORE[Ignore]
    D -->|Save Success| E[After Commit Push Attempt]
    D -->|DB Save Failure| RETRY[Retry Candidate]

    E -->|Push Success| PUSHED[PUSHED]
    E -->|Emitter Missing| DELIVERY_RETRY[Delivery Retry Candidate]
    E -->|SSE Send Failure| DELIVERY_RETRY
```

## 2. Consumer 기준 분기
```mermaid
flowchart LR
    A[Consumer Exception] --> B{Failure Type}

    B -->|JsonProcessingException / Parse Error| DLQ[DLQ]
    B -->|Unsupported eventType| DLQ
    B -->|Required Field Missing| DLQ
    B -->|Schema / Payload Contract Error| DLQ
    B -->|Value Conversion Error| DLQ

    B -->|Temporary DB Error| RETRY[Retry]
    B -->|Duplicate Event| IGNORE[Ignore]

    B -->|SSE Send Error| DELIVERY[Delivery Retry]
    B -->|Emitter Missing| DELIVERY
```

## 3. MEMBER_SIGNED_UP 흐름
```mermaid
flowchart TD
    A[member_signed_up topic message] --> B[listenMemberSignedUp]
    B --> C[parseMemberSignedUpEnvelope]
    C -->|parse failure| DLQ[DLQ]
    C -->|parse success| D[NotificationEventMapper.toCommand]
    D --> E[validateMemberSignedUp]
    E -->|invalid envelope/payload| DLQ
    E -->|valid| F[NotificationUsecase.createNotification]
    F --> G[NotificationService.saveNotification]
    G -->|duplicate eventId| IGNORE[Ignore]
    G -->|db save fail| RETRY[Retry]
    G -->|save success| H[afterCommit push]
    H -->|push success| PUSHED[PUSHED]
    H -->|emitter missing or send fail| DELIVERY[Delivery Retry Candidate]
```

## 4. ORDER_PAYMENT_RESULT 흐름
```mermaid
flowchart TD
    A[order_payment_result topic message] --> B[listenOrderPaymentResult]
    B --> C[parseOrderPaymentResultEnvelope]
    C -->|parse failure| DLQ[DLQ]
    C -->|parse success| D[validateOrderPaymentResultEvent]
    D -->|invalid envelope/payload| DLQ
    D -->|valid| E{payload.status}

    E -->|SUCCESS| F[createOrderPaymentSucceededNotification]
    E -->|FAILED| G[mapFailureReason + createOrderPaymentFailedNotification]

    F --> H[saveNotification]
    G --> H

    H -->|duplicate eventId| IGNORE[Ignore]
    H -->|db save fail| RETRY[Retry]
    H -->|save success| I[afterCommit push]
    I -->|push success| PUSHED[PUSHED]
    I -->|emitter missing or send fail| DELIVERY[Delivery Retry Candidate]
```

## 5. DLQ와 Retry의 경계
```mermaid
flowchart TB
    A[Failure Occurred] --> B{Can the same message succeed later<br/>without changing the payload?}

    B -->|No| DLQ[DLQ]
    B -->|Yes| C{Where did it fail?}

    C -->|Before notification save| RETRY[Consumer Retry Candidate]
    C -->|After notification save, during SSE delivery| DELIVERY[Delivery Retry Candidate]
```

## 6. 상태 관점 요약
```mermaid
stateDiagram-v2
    [*] --> Consumed
    Consumed --> DLQ: Parse / Validation Failure
    Consumed --> Ignored: Duplicate Event
    Consumed --> Stored: Save Success
    Consumed --> RetryCandidate: DB Save Failure

    Stored --> Pushed: SSE Send Success
    Stored --> DeliveryRetry: Emitter Missing / SSE Send Failure
```

## 7. 현재 프로젝트 기준 해석
- `DLQ`
  - 역직렬화 실패
  - 필수 필드 누락
  - event type 불일치
  - payload contract 위반
  - amount 변환 실패
- `RETRY`
  - DB 저장 실패
- `IGNORE`
  - `eventId` 기준 중복 이벤트
- `DELIVERY RETRY`
  - SSE send 예외
  - emitter 부재

## 8. 한 줄 정리
- 메시지 자체가 잘못되면 `DLQ`
- 같은 메시지를 나중에 다시 처리하면 성공할 수 있으면 `RETRY`
- 이미 처리한 이벤트면 `IGNORE`
- 알림 저장은 끝났고 브라우저 전달만 실패했으면 `DELIVERY RETRY`

## 참고 문서
- [CurrentConsumerFlowReview.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/CurrentConsumerFlowReview.md)
- [DLQImplementationPlan.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQImplementationPlan.md)
- [RetryStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryStrategy.md)
