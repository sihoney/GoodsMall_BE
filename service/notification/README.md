# Notification Service

`notification` 모듈은 Today Lunch Mall의 알림 저장, 조회, 읽음 처리, SSE 실시간 전달, Kafka 이벤트 소비를 담당하는 서비스입니다.

현재 구현 범위는 다음과 같습니다.

- 알림 데이터 저장과 목록 조회
- 읽지 않은 알림 수 조회와 읽음 처리
- SSE 기반 실시간 알림 전송
- Kafka 이벤트 소비 후 알림 메시지 생성
- 이벤트 파싱 실패 및 소비 실패 대응

이 README는 공개 진입 문서 역할만 담당합니다. 상세 설계, 이벤트 카탈로그, 재시도 전략, 운영 검토 문서는 `docs_private` 아래에 정리되어 있습니다.

## 1. 담당 역할

| 기능 | 설명 |
|---|---|
| 알림 저장 | 이벤트를 사용자 알림 레코드로 변환해 저장 |
| 알림 조회 | 목록, 페이징, 미읽음 개수 조회 |
| 읽음 처리 | 개별 또는 일괄 읽음 상태 반영 |
| 실시간 전달 | SSE 연결을 유지하고 신규 알림을 푸시 |
| 이벤트 소비 | Kafka topic 을 구독해 알림 생성 |
| 장애 대응 | 파싱 실패, 재시도, DLQ 전략을 운영 |

## 2. 주요 API 범주

| 범주 | 예시 경로 |
|---|---|
| 알림 목록 | `/api/notifications` |
| 미읽음 개수 | `/api/notifications/unread-count` |
| 읽음 처리 | `/api/notifications/{notificationId}/read` |
| SSE | `/api/notifications/sse/subscribe` |

세부 요청/응답 규격은 `docs_private` 의 API 문서를 참고합니다.

## 3. 이벤트 처리 개요

이 모듈은 회원, 주문, 결제, 정산, 경매 등에서 발행한 이벤트를 구독해 사용자 알림으로 변환합니다.

대표 처리 범위:

- 회원 가입 및 OAuth 연동
- 주문 생성, 취소, 결제 결과
- 판매자 정산 지급 결과
- 경매 낙찰, 유찰, 입찰 갱신

이벤트별 topic, eventType, 핸들러 매핑은 `docs_private` 의 이벤트 문서를 참고합니다.

## 4. 운영 관점 요약

- 알림은 저장 후 SSE 로 실시간 전달할 수 있습니다.
- 소비 실패는 재시도 또는 DLQ 정책으로 보완합니다.
- 이벤트 payload 와 공통 envelope 규약 일치가 중요합니다.
- 알림 타입별 제목, 부제목, 액션 정책은 문서 기준으로 관리합니다.

## 5. 문서 위치

상세 문서는 `notification/docs_private` 아래에 정리되어 있습니다.

대표 문서:

- `APISpec.md`
- `Architecture.md`
- `FeatureSpec.md`
- `EventDesign.md`
- `Notification-event-catalog.md`
- `Project-event-inventory.md`
- `DLQStrategy.md`
- `RetryStrategy.md`
