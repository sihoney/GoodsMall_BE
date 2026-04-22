# Notification Feature Spec

## 목표
- 도메인 이벤트를 사용자 친화적인 알림 데이터로 변환한다.
- 회원별 알림 목록, 읽지 않음 개수, 읽음 처리 기능을 제공한다.
- 저장된 알림을 SSE 기반 푸시 알림과 `/notifications` 목록 화면에서 일관된 형태로 노출한다.

## 핵심 기능

### 1. 알림 목록 조회
- 회원 본인 알림만 조회한다.
- 최신순으로 정렬한다.
- 응답 DTO는 `NotificationResponse`를 사용한다.
- 화면에서는 `title`, `subtitle`, `content`, `actions`, `elapsedTime` 중심으로 노출한다.
- 주문 번호 같은 `referenceId`는 현재 UI에서 직접 노출하지 않는다.

### 2. 읽지 않음 개수 조회
- 헤더 뱃지와 초기 화면 상태를 위해 읽지 않은 알림 개수를 제공한다.
- 조회 기준은 `member_id + is_read = false` 이다.

### 3. 알림 읽음 처리
- 회원 본인의 알림만 읽음 처리할 수 있다.
- `is_read` 값을 `true`로 갱신한다.
- 목록 페이지에서는 액션 버튼 줄의 맨 오른쪽에 `읽음 처리` 버튼을 배치한다.

### 4. 이벤트 기반 알림 생성
- 현재 구현 및 소비 대상
- 회원 가입 완료 알림
- 주문 결제 성공 알림
- 주문 결제 실패 알림
- 준비됨, 아직 consumer 미연결
- 자동 구매 확정 알림
- 판매자 정산 지급 성공 알림
- 판매자 정산 지급 실패 알림

### 5. 실시간 푸시
- 저장된 알림은 SSE로 푸시한다.
- 푸시 payload는 `NotificationResponse`를 그대로 사용한다.
- 프론트 토스트 UI는 흰 배경, 좌측 정렬, 보라 테마를 사용한다.
- 푸시 토스트와 `/notifications` 목록 카드는 동일하게 `title`, `subtitle`, `content`, `actions`, `elapsedTime` 중심으로 표현한다.

## NotificationResponse 계약

### 응답 필드
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `notificationId` | UUID | 알림 식별자 |
| `type` | `NotificationType` | 알림 종류 |
| `title` | String | 카드 제목 |
| `subtitle` | String \| null | 보조 설명. 필요 시 주문/정산 식별 정보 포함 가능 |
| `content` | String | 본문 메시지 |
| `actions` | `List<NotificationAction>` | UI 액션 버튼 정의 |
| `referenceId` | UUID \| null | 참조 리소스 ID |
| `referenceType` | `NotificationReferenceType` \| null | 참조 리소스 타입 |
| `read` | boolean | 읽음 여부 |
| `createdAt` | LocalDateTime | 생성 시각 |
| `elapsedTime` | String \| null | 경과 시간 텍스트 |
| `eventId` | UUID | 원본 이벤트 ID |
| `traceId` | String \| null | 추적용 trace ID |

### NotificationAction 필드
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `label` | String | 버튼 라벨 |
| `actionType` | String | 현재는 `navigate` 또는 `callback` |
| `routeKey` | String | 프론트 라우팅 키 |
| `referenceId` | UUID \| null | 액션 대상 ID |
| `variant` | String | 버튼 스타일. `primary`, `secondary` |

## 운영 고려 사항
- 알림 저장과 푸시는 분리하되, 푸시는 커밋 이후에 수행한다.
- `event_id` 유니크 제약을 통해 동일 이벤트 중복 저장을 막는다.
- `trace_id`를 저장해 이벤트 추적을 가능하게 한다.
- `status`와 `status_changed_at`으로 저장/푸시 상태 전이를 기록한다.
- 현재 Kafka consumer는 `MEMBER_SIGNED_UP`, `ORDER_PAYMENT_RESULT`만 실제로 구독한다.
