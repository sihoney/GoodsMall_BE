# Notification DLQ Strategy

## 목적

notification consumer 실패를 어떤 기준으로 `DLQ`, `retry`, `ignore`로 나눌지 정리한다.

## 현재 기준

- 같은 메시지를 다시 처리해도 성공 가능성이 낮으면 `DLQ`
- 다시 시도하면 성공 가능성이 있으면 `RETRY`
- 이미 처리된 이벤트이거나 중복이라면 `IGNORE`

## 실패 유형별 분류

| 실패 유형 | 권장 동작 | 설명 |
| --- | --- | --- |
| `EVENT_PARSE_FAILURE` | `DLQ` | raw message를 envelope로 파싱하지 못한 경우 |
| `UNSUPPORTED_EVENT_TYPE` | `DLQ` | handler가 없는 eventType |
| `INVALID_EVENT_PAYLOAD` | `DLQ` | 필수 필드 누락, payload contract 위반 |
| `TEMPORARY_PROCESSING_ERROR` | `RETRY` | 일시적인 처리 실패 |
| `IGNORE_DUPLICATE_EVENT` | `IGNORE` | 중복 이벤트처럼 무시해야 하는 경우 |

## Transient Failure

`transient failure`는 잠깐 발생한 일시적 실패를 뜻한다.  
조금 뒤에 같은 작업을 다시 하면 성공할 가능성이 있는 장애다.

예시는 다음과 같다.

- DB timeout
- 외부 서비스 일시 장애
- 네트워크 끊김
- 순간적인 리소스 부족

이런 실패는 보통 `RETRY` 대상이다.

반대로 아래 같은 실패는 `transient failure`가 아니다.

- JSON 파싱 실패
- 필수 필드 누락
- 지원하지 않는 eventType
- contract 위반

이런 실패는 다시 시도해도 성공 가능성이 낮으므로 보통 즉시 `DLQ`로 보낸다.

## DLQ 동작

- `DLQ`이면 Kafka DLQ topic으로 발행한다.
- `RETRY`이면 예외를 다시 던져 상위 retry 정책이 처리하도록 한다.
- `IGNORE`이면 조용히 종료한다.

## DLQ Topic

- 기본 topic 이름: `notification.dlq`
- 설정 키: `notification.kafka.topics.dlq`

## DLQ Message Schema

현재 DLQ payload는 아래 정보를 포함한다.

| Field | Description |
| --- | --- |
| `listenerName` | 실패가 발생한 listener 이름 |
| `reason` | `NotificationDlqReason` 값 |
| `exceptionType` | 예외 클래스 이름 |
| `exceptionMessage` | 예외 메시지 |
| `rawMessage` | 원본 Kafka message |
| `failedAt` | DLQ 발행 시각 |

## Publish Failure Policy

- DLQ message 직렬화 실패 시 error log fallback
- Kafka DLQ publish 실패 시 error log fallback

## 관련 문서

- [DLQRetryPolicy.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQRetryPolicy.md)
- [DLQImplementationPlan.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQImplementationPlan.md)
- [CurrentConsumerFlowReview.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/CurrentConsumerFlowReview.md)
