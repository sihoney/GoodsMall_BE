# Notification API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/notifications` | 내 알림 목록 조회 | Header: 인증 정보, Query: `page`, `size` | 완료 |
| `GET` | `/api/notifications/unread-count` | 미읽음 알림 개수 조회 | Header: 인증 정보 | 완료 |
| `PATCH` | `/api/notifications/{notificationId}/read` | 알림 읽음 처리 | Header: 인증 정보, Path: `notificationId` | 완료 |

## 공통 응답 형식
```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## 1. 내 알림 목록 조회
### `GET /api/notifications?page=0&size=20`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |
| Query | `page` | Integer | N |
| Query | `size` | Integer | N |

#### Request Example
```text
GET /api/notifications?page=0&size=20
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "notificationId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "memberId": "11111111-1111-1111-1111-111111111111",
        "type": "ORDER_PAYMENT_SUCCEEDED",
        "title": "Payment completed",
        "content": "Your payment was completed successfully. Amount: 15000",
        "referenceId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
        "referenceType": "ORDER",
        "read": false,
        "createdAt": "2026-04-10T12:10:00"
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

## 2. 미읽음 알림 개수 조회
### `GET /api/notifications/unread-count`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |

#### Response JSON
```json
{
  "success": true,
  "data": {
    "unreadCount": 3
  },
  "error": null
}
```

## 3. 알림 읽음 처리
### `PATCH /api/notifications/{notificationId}/read`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |
| Path | `notificationId` | UUID | Y |

#### Request Example
```text
PATCH /api/notifications/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/read
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "notificationId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "memberId": "11111111-1111-1111-1111-111111111111",
    "type": "ORDER_PAYMENT_SUCCEEDED",
    "title": "Payment completed",
    "content": "Your payment was completed successfully. Amount: 15000",
    "referenceId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "referenceType": "ORDER",
    "read": true,
    "createdAt": "2026-04-10T12:10:00"
  },
  "error": null
}
```

## 이벤트 기반 Payload 예시
아래는 공개 REST API가 아니라 Kafka consumer가 받는 payload 예시입니다.

### 자동 구매 확정 이벤트

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Payload | `orderId` | UUID | Y |
| Payload | `buyerMemberId` | UUID | Y |
| Payload | `confirmedAt` | Instant | Y |

```json
{
  "orderId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "buyerMemberId": "11111111-1111-1111-1111-111111111111",
  "confirmedAt": "2026-04-10T03:15:00Z"
}
```

### 주문 결제 결과 이벤트

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Payload | `eventId` | UUID | Y |
| Payload | `orderId` | UUID | Y |
| Payload | `buyerMemberId` | UUID | Y |
| Payload | `amount` | Number | 조건부 |
| Payload | `status` | Enum | Y |
| Payload | `reasonCode` | Enum | 조건부 |
| Payload | `occurredAt` | Instant | Y |

```json
{
  "eventId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "orderId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "buyerMemberId": "11111111-1111-1111-1111-111111111111",
  "amount": 15000,
  "status": "SUCCESS",
  "reasonCode": null,
  "occurredAt": "2026-04-10T03:20:00Z"
}
```

### 판매자 정산 지급 결과 이벤트

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Payload | `eventId` | UUID | Y |
| Payload | `requestEventId` | UUID | Y |
| Payload | `settlementId` | UUID | Y |
| Payload | `sellerMemberId` | UUID | Y |
| Payload | `payoutAmount` | Long | Y |
| Payload | `resultStatus` | Enum | Y |
| Payload | `failureReason` | Enum | 조건부 |
| Payload | `processedAt` | LocalDateTime | Y |

```json
{
  "eventId": "dddddddd-dddd-dddd-dddd-dddddddddddd",
  "requestEventId": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
  "settlementId": "ffffffff-ffff-ffff-ffff-ffffffffffff",
  "sellerMemberId": "22222222-2222-2222-2222-222222222222",
  "payoutAmount": 30000,
  "resultStatus": "FAILED",
  "failureReason": "WALLET_NOT_FOUND",
  "processedAt": "2026-04-10T12:20:00"
}
```

## 에러 응답 예시
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "NOTIFICATION_NOT_FOUND",
    "message": "알림을 찾을 수 없습니다."
  }
}
```
