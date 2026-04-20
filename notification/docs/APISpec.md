# Notification API Spec

## Endpoint Summary
| Method | Endpoint | Description | Implemented |
| --- | --- | --- | --- |
| `GET` | `/api/notifications` | 내 알림 목록 조회 | `✓` |
| `GET` | `/api/notifications/unread-count` | 미읽음 알림 개수 조회 | `✓` |
| `PATCH` | `/api/notifications/{notificationId}/read` | 알림 읽음 처리 | `✓` |
| `GET` | `/api/notifications/stream` | SSE 알림 구독 | `✓` |

## Common Response Shape

### REST API
```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## NotificationResponse Fields
| Field | Type | Description |
| --- | --- | --- |
| `notificationId` | UUID | 알림 식별자 |
| `type` | `NotificationType` | 알림 종류 |
| `title` | String | 알림 제목 |
| `subtitle` | String \| null | 보조 설명 |
| `content` | String | 알림 본문 |
| `actions` | `List<NotificationAction>` | UI 액션 버튼 목록 |
| `referenceId` | UUID \| null | 참조 대상 ID |
| `referenceType` | `NotificationReferenceType` \| null | 참조 대상 타입 |
| `read` | boolean | 읽음 여부 |
| `createdAt` | LocalDateTime | 생성 시각 |
| `elapsedTime` | String \| null | 경과 시간 텍스트 |
| `eventId` | UUID | 원본 이벤트 ID |
| `traceId` | String \| null | 추적용 trace ID |

## NotificationAction Fields
| Field | Type | Description |
| --- | --- | --- |
| `label` | String | 버튼 레이블 |
| `actionType` | String | `navigate`, `callback` |
| `routeKey` | String | 프론트 라우팅 키 |
| `referenceId` | UUID \| null | 액션 대상 ID |
| `variant` | String | `primary`, `secondary` |

## SSE Notes
- `/api/notifications/stream`은 서버-클라이언트 단방향 SSE 연결이다.
- 서버는 연결 직후 `connected` 이벤트를 한 번 보낸다.
- 알림 저장 후 `afterCommit` 시점에 실시간 push를 시도한다.
- `PUSHED`는 서버 기준 `SseEmitter.send(...)` 성공을 의미하며, 브라우저 렌더링 완료를 보장하지는 않는다.
- emitter 정리 시에는 `memberId`만이 아니라 emitter instance도 함께 확인해 stale callback이 새 연결을 제거하지 않도록 처리한다.

## 1. Notification List

### `GET /api/notifications?page=0&size=20`

#### Request Example
```text
GET /api/notifications?page=0&size=20
Authorization: Bearer {accessToken}
```

#### Response Example
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "notificationId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "type": "ORDER_PAYMENT_SUCCEEDED",
        "title": "Payment completed",
        "subtitle": "Order bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "content": "Your payment was completed successfully.",
        "actions": [
          {
            "label": "Order detail",
            "actionType": "navigate",
            "routeKey": "ORDER_DETAIL",
            "referenceId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            "variant": "primary"
          }
        ],
        "referenceId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "referenceType": "ORDER",
        "read": false,
        "createdAt": "2026-04-10T12:10:00",
        "elapsedTime": "5 minutes ago",
        "eventId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
        "traceId": "trace-order-payment-001"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  },
  "error": null
}
```

## 2. Unread Count

### `GET /api/notifications/unread-count`

#### Response Example
```json
{
  "success": true,
  "data": {
    "unreadCount": 3
  },
  "error": null
}
```

## 3. Mark As Read

### `PATCH /api/notifications/{notificationId}/read`

#### Request Example
```text
PATCH /api/notifications/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/read
Authorization: Bearer {accessToken}
```

#### Response Example
```json
{
  "success": true,
  "data": {
    "notificationId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "type": "ORDER_PAYMENT_SUCCEEDED",
    "title": "Payment completed",
    "subtitle": "Order bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "content": "Your payment was completed successfully.",
    "actions": [],
    "referenceId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "referenceType": "ORDER",
    "read": true,
    "createdAt": "2026-04-10T12:10:00",
    "elapsedTime": "5 minutes ago",
    "eventId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "traceId": "trace-order-payment-001"
  },
  "error": null
}
```

## 4. SSE Stream

### `GET /api/notifications/stream`

#### Response Behavior
- SSE 연결이 열리면 서버는 `connected` 이벤트를 먼저 보낸다.
- 이후 실시간 알림은 `notification` 이벤트로 push된다.

#### Example Event Stream
```text
event: connected
data: {"memberId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"}

event: notification
data: {"notificationId":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","type":"MEMBER_SIGNED_UP","title":"Welcome to TodayLunch"}
```

## 참고 문서
- [Architecture.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/Architecture.md)
- [EventDesign.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/EventDesign.md)
- [StateTransition.md](C:/my_project/beadv5_2_TodayLunchMenu_BE/notification/docs/StateTransition.md)
