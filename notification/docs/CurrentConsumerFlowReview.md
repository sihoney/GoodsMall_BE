# Notification Current Consumer Flow Review

## 목적
- 현재 notification 서비스의 Kafka consumer 흐름을 코드 기준으로 다시 정리한다.
- 어떤 이벤트가 실제로 소비되는지, 어디서 검증되는지, 어디서 실패할 수 있는지 명확히 한다.
- 이후 DLQ / retry 분류 기준의 입력 자료로 사용한다.

## 현재 실제 소비 대상 이벤트
현재 코드 기준으로 notification 서비스가 실제로 Kafka에서 소비하는 이벤트는 2개다.

1. `MEMBER_SIGNED_UP`
2. `ORDER_PAYMENT_RESULT`

근거:
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L42)
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L52)

## 전체 흐름 개요

### 1. MEMBER_SIGNED_UP 흐름
1. Kafka listener가 raw `String message`를 받는다.
2. `parseMemberSignedUpEnvelope(message)`에서 JSON을 `EventEnvelope<MemberSignedUpPayload>`로 역직렬화한다.
3. `NotificationEventMapper.toCommand(...)`에서 이벤트를 검증하고 `NotificationCommand`로 변환한다.
4. `notificationUsecase.createNotification(command)`를 호출한다.
5. `NotificationService`가 알림 저장 및 중복 방지를 처리한다.
6. 트랜잭션 커밋 이후 `NotificationPushService.push(...)`가 SSE push를 시도한다.

코드 위치:
- consumer entry: [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L47)
- mapper validation: [NotificationEventMapper.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/mapper/NotificationEventMapper.java#L19)
- service save: [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L80)
- push after commit: [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L285)
- push execution: [NotificationPushService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationPushService.java#L28)

### 2. ORDER_PAYMENT_RESULT 흐름
1. Kafka listener가 raw `String message`를 받는다.
2. `parseOrderPaymentResultEnvelope(message)`에서 JSON을 `EventEnvelope<OrderPaymentResultMessage>`로 역직렬화한다.
3. `validateOrderPaymentResultEvent(event)`에서 envelope/payload를 검증한다.
4. `occurredAt`을 `Asia/Seoul` 기준 `LocalDateTime`으로 변환한다.
5. `payload.status()`가 `SUCCESS`면 `createOrderPaymentSucceededNotification(...)` 호출
6. `FAILED`면 `reasonCode`를 내부 enum으로 매핑한 뒤 `createOrderPaymentFailedNotification(...)` 호출
7. 이후 저장, 중복 방지, after-commit push 흐름은 동일하다.

코드 위치:
- consumer entry: [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L57)
- validation: [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L101)
- amount conversion: [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L150)
- failure reason mapping: [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L158)

## 계층별 책임

### Consumer 계층
- Kafka 메시지 수신
- JSON 역직렬화
- 일부 이벤트의 envelope/payload 검증
- 이벤트 타입에 따라 적절한 application usecase 호출

관련 코드:
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java)

### Mapper 계층
- `MEMBER_SIGNED_UP` 이벤트 검증
- `NotificationCommand` 생성

관련 코드:
- [NotificationEventMapper.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/mapper/NotificationEventMapper.java)

특징:
- `MEMBER_SIGNED_UP`는 검증 로직이 consumer가 아니라 mapper 안에 있다.
- 반면 `ORDER_PAYMENT_RESULT`는 consumer 내부에서 직접 검증한다.
- 즉, 현재는 이벤트별 검증 위치가 일관되지 않다.

### Service 계층
- 공통 인자 검증
- 중복 이벤트 방지 (`existsByEventId`)
- 알림 저장
- after-commit 기반 SSE push 트리거

관련 코드:
- [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L253)

### Push 계층
- 알림 ID 기준 알림 재조회
- 이미 `PUSHED` 상태면 no-op
- emitter 존재 시 SSE 전송
- 성공 시 `PUSHED`, 실패 시 `FAILED`
- emitter 부재 시 현재는 상태 변경 없이 로그만 남기고 종료

관련 코드:
- [NotificationPushService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationPushService.java)

## 현재 실패 지점 정리

### 1. Kafka message 역직렬화 실패

발생 위치:
- `parseMemberSignedUpEnvelope(...)`
- `parseOrderPaymentResultEnvelope(...)`

코드 근거:
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L85)
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L93)

현재 동작:
- `JsonProcessingException`을 `IllegalArgumentException`으로 감싸서 던진다.

해석:
- 같은 payload를 재시도해도 성공 가능성이 낮다.
- DLQ 후보로 보기 좋은 실패 유형이다.

### 2. Envelope / payload validation 실패

발생 위치:
- `NotificationEventMapper.validateMemberSignedUp(...)`
- `NotificationEventConsumer.validateOrderPaymentResultEvent(...)`

코드 근거:
- [NotificationEventMapper.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/mapper/NotificationEventMapper.java#L36)
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L101)

검증 예시:
- `eventId` 없음
- `eventType` 불일치
- `source` 없음
- `recipientId` 없음
- `payload` 없음
- `payload.memberId`, `payload.orderId`, `payload.amount` 없음
- `recipientId`와 payload member ID 불일치

해석:
- 대부분 계약 위반 또는 복구 불가능한 데이터 오류다.
- DLQ 직행 후보로 보는 것이 자연스럽다.

### 3. 값 변환 실패

발생 위치:
- `toLongAmount(BigDecimal amount)`

코드 근거:
- [NotificationEventConsumer.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java#L150)

현재 동작:
- 소수점 금액 등 whole number가 아니면 `IllegalArgumentException` 발생

해석:
- payload contract 위반에 가깝다.
- 재시도보다 DLQ가 적절한 후보다.

### 4. Application service 입력 검증 실패

발생 위치:
- `NotificationService.validateCommonArguments(...)`
- `validateOrderArguments(...)`
- 개별 create 메서드의 null/양수 검증

코드 근거:
- [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L310)
- [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L322)

현재 동작:
- `IllegalArgumentException` 발생

해석:
- 대부분 upstream 데이터 계약 위반이므로 DLQ 후보다.
- 다만 service 내부 검증과 consumer/mapper 검증이 중복되는 부분이 있어 향후 정리가 필요하다.

### 5. DB 저장 실패

발생 위치:
- `notificationJpaRepository.save(notification)`

코드 근거:
- [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L276)

현재 동작:
- `RuntimeException`을 다시 던진다.
- 최소 운영 지표 단계에서는 `notification_save_failed_total` 카운트만 증가시킨다.

해석:
- 일시적 DB 문제일 수 있다.
- 재시도 후보로 분류할 가능성이 높다.

### 6. 중복 이벤트

발생 위치:
- `existsByEventId(eventId)`

코드 근거:
- [NotificationService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationService.java#L267)

현재 동작:
- 예외 없이 무시
- 로그 기록
- duplicate metric 기록

해석:
- DLQ나 retry 대상이 아니다.
- `IGNORE` 분류가 적절하다.

### 7. SSE push 실패

발생 위치:
- `NotificationPushService.sendAndMark(...)`

코드 근거:
- [NotificationPushService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationPushService.java#L65)

현재 동작:
- `emitter.send(...)` 예외 시 알림 상태를 `FAILED`로 변경
- emitter 제거
- warn 로그 기록
- push failure metric 기록

해석:
- Kafka consumer 단계의 실패는 아니다.
- 향후 retry 전략의 주요 대상이다.
- DLQ보다 notification retry 영역으로 보는 것이 맞다.

### 8. emitter 부재

발생 위치:
- `NotificationPushService.push(...)`

코드 근거:
- [NotificationPushService.java](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/src/main/java/com/example/notification/application/service/NotificationPushService.java#L51)

현재 동작:
- 상태 변경 없이 종료
- emitter missing metric 기록

해석:
- consumer 실패가 아니라 delivery gap에 가깝다.
- DLQ 대상은 아니고, retry 또는 polling 기반 재전송 정책 대상이다.

## 현재 흐름의 특징

### 1. 검증 위치가 분산돼 있다
- `MEMBER_SIGNED_UP`는 mapper에서 검증
- `ORDER_PAYMENT_RESULT`는 consumer에서 검증

영향:
- DLQ 분류 로직을 도입할 때 예외 발생 지점이 흩어져 보일 수 있다.
- 향후 classifier 도입 시 consumer 경계에서 예외를 한 번에 분류하는 구조가 필요하다.

### 2. consumer와 push 실패는 다른 성격이다
- consumer 실패
  - 메시지 해석 자체가 안 됨
  - payload 계약 위반
  - application service 입력 검증 실패
- push 실패
  - 알림 저장은 완료됨
  - delivery 단계에서 실패

영향:
- consumer 실패는 DLQ 우선
- push 실패는 retry 우선

### 3. 현재는 DLQ 분기 구조가 없다
- 예외가 발생하면 분류 없이 상위로 전파된다.
- 어떤 예외가 DLQ 대상인지 코드에서 명시돼 있지 않다.

### 4. 중복 이벤트는 이미 `IGNORE` 정책이 있다
- `eventId` 기준으로 중복은 예외 없이 무시된다.
- 이는 DLQ/retry가 아니라 멱등성 처리 범주다.

## 실패 지점별 초기 분류 제안
| 실패 지점 | 현재 예외 형태 | 초기 분류 제안 |
| --- | --- | --- |
| JSON 역직렬화 실패 | `IllegalArgumentException` | `DLQ` |
| eventType 불일치 | `IllegalArgumentException` | `DLQ` |
| 필수 필드 누락 | `IllegalArgumentException` | `DLQ` |
| 금액 변환 실패 | `IllegalArgumentException` | `DLQ` |
| service 입력 검증 실패 | `IllegalArgumentException` | `DLQ` |
| DB 저장 실패 | `RuntimeException` | `RETRY` |
| 중복 이벤트 | no-op | `IGNORE` |
| SSE send 예외 | 상태 `FAILED` | `RETRY` |
| emitter 부재 | 상태 유지 | `RETRY` 또는 별도 delivery 정책 |

## 이번 재확인 단계의 결론
1. 현재 실제 consumer 대상 이벤트는 `MEMBER_SIGNED_UP`, `ORDER_PAYMENT_RESULT` 두 개다.
2. consumer 계층에서 발생하는 대부분의 `IllegalArgumentException`은 payload/contract 오류 성격이 강하다.
3. 저장 이후의 SSE push 실패는 consumer DLQ보다 retry 영역으로 다뤄야 한다.
4. 중복 이벤트는 이미 `IGNORE` 정책이 있으므로 DLQ/retry 대상이 아니다.
5. 다음 단계에서는 consumer 경계에서 예외를 `DLQ / RETRY / IGNORE`로 분류하는 classifier 구조를 도입하는 것이 자연스럽다.

## 다음 단계 추천
1. `NotificationConsumerFailureDecision` 정의
2. `NotificationConsumerExceptionClassifier` 추가
3. `NotificationDlqPublisher` 인터페이스 추가
4. `LoggingNotificationDlqPublisher` 기본 구현 추가
5. consumer 테스트에서 실패 유형별 분기 검증 추가

## 참고 문서
- [DLQImplementationPlan.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQImplementationPlan.md)
- [RetryStrategy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/RetryStrategy.md)
- [StateTransition.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/StateTransition.md)
