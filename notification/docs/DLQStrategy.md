# Notification DLQ Strategy

## 목적
- notification consumer 실패를 어떤 기준으로 DLQ, retry, ignore로 나누는지 정리한다.

## 현재 기본 원칙
- 같은 메시지를 다시 처리해도 성공 가능성이 낮으면 `DLQ`
- 다시 시도하면 성공 가능성이 있으면 `RETRY`
- 이미 처리했거나 정책상 버려도 되는 경우는 `IGNORE`

## 현재 reason 체계
| Reason | Action | Description |
| --- | --- | --- |
| `EVENT_PARSE_FAILURE` | `DLQ` | raw message -> envelope parse 실패 |
| `UNSUPPORTED_EVENT_TYPE` | `DLQ` | handler가 없는 eventType |
| `INVALID_EVENT_PAYLOAD` | `DLQ` | 필수 필드 누락, payload contract 위반 |
| `TEMPORARY_PROCESSING_ERROR` | `RETRY` | 일시적 런타임 오류 |
| `IGNORE_DUPLICATE_EVENT` | `IGNORE` | 중복 이벤트 등 무시 대상 |

## 현재 구현
- consumer는 `NotificationConsumerExceptionClassifier`를 통해 failure decision을 얻는다.
- `DLQ`면 Kafka DLQ topic으로 발행한다.
- `RETRY`면 예외를 재던진다.
- `IGNORE`면 조용히 종료한다.

## DLQ Topic
- 기본 topic 이름:
  - `notification.dlq`
- 프로퍼티:
  - `notification.kafka.topics.dlq`

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

## 현재 범위 밖 항목
- DLQ replay 도구
- DLQ topic 소비 대시보드
- DLQ 메시지 재처리 정책

## 관련 문서
- [DLQDiagram.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQDiagram.md)
- [DLQImplementationPlan.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/DLQImplementationPlan.md)
- [CurrentConsumerFlowReview.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/CurrentConsumerFlowReview.md)
