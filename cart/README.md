# API Specification - Cart Module

> 도서 이커머스 플랫폼 API 명세서
> Cart API (/api/carts/*), Wish API (/api/wishes/*)

---

## 📌 Cart API

| 기능 | Method | Endpoint | 구현 | 설명 |
|------|--------|----------|:---:|------|
| 장바구니 조회 | GET | /api/carts | ✅ | 내 장바구니 조회 |
| 장바구니 비우기 | DELETE | /api/carts | ✅ | 장바구니 전체 삭제 |
| 장바구니 상품 추가 | POST | /api/carts/items | ✅ | 상품 1개 추가 (최대 10개) |
| 장바구니 수량 변경 | PATCH | /api/carts/items/{cartId} | ✅ | 수량 변경 |
| 장바구니 상품 삭제 | DELETE | /api/carts/items | ✅ | 선택 삭제 (다건) |
| 주문 완료 상품 제거 | DELETE | /api/carts/complete-ordered | ✅ | 주문 완료 후 정리 (내부 통신) |

> 장바구니는 최대 **10개**까지 담을 수 있습니다.

---

### `GET /api/carts` — 장바구니 조회

인증된 사용자의 장바구니를 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440001",
  "items": [
    {
      "cartId": "770e8400-e29b-41d4-a716-446655440001",
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "addedAt": "2026-03-01T09:00:00"
    }
  ],
  "itemCount": 1
}
```

---

### `DELETE /api/carts` — 장바구니 비우기

장바구니의 모든 상품을 삭제합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

---

### `POST /api/carts/items` — 장바구니 상품 추가

장바구니에 상품을 추가합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productId` | UUID | O | 상품 ID |
| `quantity` | Integer | O | 수량 (최소 1) |

Request Body Example:

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 2
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440001",
  "items": [
    {
      "cartId": "770e8400-e29b-41d4-a716-446655440001",
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "addedAt": "2026-03-01T09:00:00"
    }
  ],
  "itemCount": 1
}
```

**2. 클라이언트 오류 — 입력값 검증 실패**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "입력값이 올바르지 않습니다",
  "errorCode": "INVALID_INPUT_VALUE",
  "path": "/api/carts/items",
  "timestamp": "2026-03-01T09:00:00",
  "errors": [
    {
      "field": "productId",
      "rejectedValue": null,
      "message": "상품 ID는 필수입니다"
    }
  ]
}
```

**3. 클라이언트 오류 — 중복 상품**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "이미 장바구니에 있는 상품입니다",
  "errorCode": "CART_ITEM_DUPLICATE",
  "path": "/api/carts/items",
  "timestamp": "2026-03-01T09:00:00"
}
```

**4. 클라이언트 오류 — 장바구니 한도 초과**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "장바구니에는 최대 10개까지 담을 수 있습니다",
  "errorCode": "CART_LIMIT_EXCEEDED",
  "path": "/api/carts/items",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `PATCH /api/carts/items/{cartId}` — 장바구니 수량 변경

장바구니 항목의 수량을 변경합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartId` | UUID | O | 장바구니 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `quantity` | Integer | O | 변경할 수량 (최소 1) |

Request Body Example:

```json
{
  "quantity": 5
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440001",
  "items": [
    {
      "cartId": "770e8400-e29b-41d4-a716-446655440001",
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 5,
      "addedAt": "2026-03-01T09:00:00"
    }
  ],
  "itemCount": 1
}
```

**2. 클라이언트 오류 — 장바구니 항목 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "장바구니 항목을 찾을 수 없습니다",
  "errorCode": "CART_ITEM_NOT_FOUND",
  "path": "/api/carts/items/770e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `DELETE /api/carts/items` — 장바구니 상품 삭제

선택한 장바구니 항목들을 삭제합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `cartIds` | UUID[] | O | 삭제할 장바구니 항목 ID 목록 |

Request Body Example:

```json
{
  "cartIds": [
    "770e8400-e29b-41d4-a716-446655440001",
    "770e8400-e29b-41d4-a716-446655440002"
  ]
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440001",
  "items": [],
  "itemCount": 0
}
```

**2. 클라이언트 오류 — 빈 목록 전달**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "입력값이 올바르지 않습니다",
  "errorCode": "INVALID_INPUT_VALUE",
  "path": "/api/carts/items",
  "timestamp": "2026-03-01T09:00:00",
  "errors": [
    {
      "field": "cartIds",
      "rejectedValue": [],
      "message": "장바구니 아이템 ID 목록은 필수입니다"
    }
  ]
}
```

---

### `DELETE /api/carts/complete-ordered` — 주문 완료 상품 제거

주문이 완료된 상품을 장바구니에서 제거합니다. 서비스 간 내부 통신용 (Kafka Consumer 연동 예정).

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | UUID | O | 회원 ID |
| `productIds` | UUID[] | O | 제거할 상품 ID 목록 |

Request Body Example:

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440001",
  "productIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440003"
  ]
}
```

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

---

## 📌 Wish API (찜)

| 기능 | Method | Endpoint | 구현 | 설명 |
|------|--------|----------|:---:|------|
| 찜 목록 조회 | GET | /api/wishes | ✅ | 내 찜 목록 |
| 찜 토글 | POST | /api/wishes/{productId} | ✅ | 찜 추가/해제 토글 |
| 찜 → 장바구니 이동 | POST | /api/wishes/{wishId}/to-cart | ✅ | 찜 상품을 장바구니로 이동 |

---

### `GET /api/wishes` — 찜 목록 조회

인증된 사용자의 찜 목록을 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "productIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440003"
  ],
  "totalCount": 2
}
```

---

### `POST /api/wishes/{productId}` — 찜 토글

상품을 찜 목록에 추가하거나 해제합니다. 이미 찜한 상품이면 해제, 찜하지 않은 상품이면 추가됩니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

#### Response

**1. 요청 성공 — 찜 추가**

Status Code: `200 OK`

```json
{
  "added": true
}
```

**2. 요청 성공 — 찜 해제**

Status Code: `200 OK`

```json
{
  "added": false
}
```

---

### `POST /api/wishes/{wishId}/to-cart` — 찜 → 장바구니 이동

찜한 상품을 장바구니로 이동합니다. 수량 1개로 장바구니에 추가되며, 찜 목록에서 제거됩니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `wishId` | UUID | O | 찜 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

**2. 클라이언트 오류 — 찜 항목 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "찜을 찾을 수 없습니다",
  "errorCode": "WISH_NOT_FOUND",
  "path": "/api/wishes/770e8400-.../to-cart",
  "timestamp": "2026-03-01T09:00:00"
}
```

**3. 권한 오류 — 본인의 찜이 아닌 경우**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "해당 찜에 접근할 권한이 없습니다",
  "errorCode": "MEMBER_NOT_AUTHORIZED",
  "path": "/api/wishes/770e8400-.../to-cart",
  "timestamp": "2026-03-01T09:00:00"
}
```

**4. 클라이언트 오류 — 이미 장바구니에 있는 상품**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "이미 장바구니에 있는 상품입니다",
  "errorCode": "CART_ITEM_DUPLICATE",
  "path": "/api/wishes/770e8400-.../to-cart",
  "timestamp": "2026-03-01T09:00:00"
}
```

**5. 클라이언트 오류 — 장바구니 한도 초과**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "장바구니에는 최대 10개까지 담을 수 있습니다",
  "errorCode": "CART_LIMIT_EXCEEDED",
  "path": "/api/wishes/770e8400-.../to-cart",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

## 📌 공통 에러 코드

### Cart 에러

| ErrorCode | HTTP Status | 메시지 |
|-----------|-------------|--------|
| `INVALID_INPUT_VALUE` | 400 | 입력값이 올바르지 않습니다 |
| `CART_ITEM_NOT_FOUND` | 404 | 장바구니 항목을 찾을 수 없습니다 |
| `CART_ITEM_DUPLICATE` | 409 | 이미 장바구니에 있는 상품입니다 |
| `CART_LIMIT_EXCEEDED` | 409 | 장바구니에는 최대 10개까지 담을 수 있습니다 |

### Wish 에러

| ErrorCode | HTTP Status | 메시지 |
|-----------|-------------|--------|
| `INVALID_INPUT_VALUE` | 400 | 입력값이 올바르지 않습니다 |
| `WISH_NOT_FOUND` | 404 | 찜을 찾을 수 없습니다 |
| `MEMBER_NOT_AUTHORIZED` | 403 | 해당 찜에 접근할 권한이 없습니다 |
