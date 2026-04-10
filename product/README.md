# API Specification - Product Module

> 도서 이커머스 플랫폼 API 명세서
> Product API (/api/products/*), Category API (/api/categories/*), Image API

---

## 📌 Product API

> ★ = 상품선택 → 결제완료 → 정산완료 필수 플로우

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 상품 목록 조회 | GET | /api/products | ✅ | ★ | 카테고리/키워드/가격 필터 검색 |
| 인기 상품 조회 | GET | /api/products/popular | ✅ | | 조회수 기반 인기 상품 |
| 전체 상품 조회 (관리자) | GET | /api/products/admin/all | ✅ | | 전체 상품 (ADMIN 전용) |
| 판매자 상품 조회 | GET | /api/products/seller | ✅ | | 본인 등록 상품 (SELLER 전용) |
| 상품 상세 조회 | GET | /api/products/{productId} | ✅ | ★ | 상품 상세 + 조회수 증가 |
| 상품 ID 목록 조회 | GET | /api/products/by-ids | ✅ | | 여러 상품 한번에 조회 |
| 상품 등록 | POST | /api/products | ✅ | ★ | 상품 + 이미지 등록 (SELLER 전용) |
| 재고 확인 및 차감 | POST | /api/products/check-availability | ✅ | ★ | 주문 시 재고 확인/차감 (분산락) |
| 상품 수정 | PUT | /api/products/{productId} | ✅ | | 상품 정보 수정 (SELLER 전용) |
| 상품 삭제 | DELETE | /api/products/{productId} | ✅ | | 소프트 삭제 (SELLER 전용) |
| 상품 복원 | POST | /api/products/{productId}/restore | ✅ | | 삭제된 상품 복원 (SELLER 전용) |
| 재고 증가 | PATCH | /api/products/{productId}/stock/increase | ✅ | | 재고 수량 증가 (SELLER 전용) |
| 재고 감소 | PATCH | /api/products/{productId}/stock/decrease | ✅ | | 재고 수량 감소 (SELLER 전용) |
| 상품 상태 변경 | PATCH | /api/products/{productId}/status | ✅ | ★ | 상태 전환 (SELLER 전용) |

---

### `GET /api/products` — 상품 목록 조회

카테고리, 키워드, 가격 범위 필터를 지원하는 상품 목록 검색입니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | String (UUID) | X | 카테고리 ID (하위 카테고리 포함 검색) |
| `keyword` | String | X | 검색 키워드 (제목 검색) |
| `minPrice` | BigDecimal | X | 최소 가격 |
| `maxPrice` | BigDecimal | X | 최대 가격 |
| `page` | Integer | X | 페이지 번호 (기본값: 0) |
| `size` | Integer | X | 페이지 크기 (기본값: 20) |
| `sort` | String | X | 정렬 (예: `price,asc`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "content": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "title": "클린 코드",
      "description": "로버트 C. 마틴의 클린 코드",
      "price": 33000,
      "count": 50,
      "status": "ACTIVE",
      "categoryId": "550e8400-e29b-41d4-a716-446655440002",
      "categoryName": "프로그래밍",
      "createdAt": "2026-03-01T09:00:00",
      "images": []
    }
  ],
  "pageable": { ... },
  "totalElements": 100,
  "totalPages": 5
}
```

---

### `GET /api/products/popular` — 인기 상품 조회

조회수 기반 인기 상품을 조회합니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | Integer | X | 페이지 번호 (기본값: 0) |
| `size` | Integer | X | 페이지 크기 (기본값: 20) |

#### Response

Status Code: `200 OK`

응답 형식은 상품 목록 조회와 동일합니다.

---

### `GET /api/products/admin/all` — 전체 상품 조회 (관리자)

모든 상품을 조회합니다. ADMIN 권한 필요.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | Integer | X | 페이지 번호 (기본값: 0) |
| `size` | Integer | X | 페이지 크기 (기본값: 20) |

#### Response

Status Code: `200 OK`

응답 형식은 상품 목록 조회와 동일합니다.

---

### `GET /api/products/seller` — 판매자 상품 조회

본인이 등록한 상품 목록을 조회합니다. SELLER 권한 필요.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | Integer | X | 페이지 번호 (기본값: 0) |
| `size` | Integer | X | 페이지 크기 (기본값: 20) |

#### Response

Status Code: `200 OK`

응답 형식은 상품 목록 조회와 동일합니다.

---

### `GET /api/products/{productId}` — 상품 상세 조회

특정 상품의 상세 정보를 조회합니다. 조회 시 조회수가 1 증가합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 조회할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "클린 코드",
  "description": "로버트 C. 마틴의 클린 코드",
  "price": 33000,
  "count": 50,
  "status": "ACTIVE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "프로그래밍",
  "createdAt": "2026-03-01T09:00:00",
  "images": [
    {
      "imageId": "660e8400-e29b-41d4-a716-446655440001",
      "s3Key": "products/image1.jpg",
      "presignedUrl": "https://s3.amazonaws.com/...",
      "sortOrder": 0,
      "isThumbnail": true,
      "createdAt": "2026-03-01T09:00:00"
    }
  ]
}
```

**2. 클라이언트 오류 — 존재하지 않는 상품**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "상품을 찾을 수 없습니다",
  "errorCode": "PRODUCT_NOT_FOUND",
  "path": "/api/products/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `GET /api/products/by-ids` — 상품 ID 목록 조회

여러 상품 ID로 상품 목록을 한번에 조회합니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productIds` | UUID[] | O | 조회할 상품 ID 목록 |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
[
  {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "클린 코드",
    "description": "로버트 C. 마틴의 클린 코드",
    "price": 33000,
    "count": 50,
    "status": "ACTIVE",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "categoryName": "프로그래밍",
    "createdAt": "2026-03-01T09:00:00",
    "images": []
  }
]
```

---

### `POST /api/products` — 상품 등록

새로운 상품을 등록합니다. Multipart 요청으로 상품 데이터와 이미지를 함께 업로드합니다. SELLER 권한 필요.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | String | O | `multipart/form-data` |
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Request Parts:

| 파트 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productData` | String (JSON) | O | 상품 정보 JSON |
| `images` | MultipartFile[] | X | 상품 이미지 파일 (최대 10개) |
| `thumbnailIndex` | Integer | X | 썸네일 이미지 인덱스 (기본값: 0) |

productData JSON:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | O | 상품명 |
| `description` | String | X | 상품 설명 |
| `price` | BigDecimal | O | 판매 가격 (0보다 커야 함) |
| `stockQuantity` | Integer | O | 초기 재고 수량 (0 이상) |
| `categoryId` | UUID | O | 카테고리 ID |

productData Example:

```json
{
  "title": "클린 코드",
  "description": "로버트 C. 마틴의 클린 코드",
  "price": 33000,
  "stockQuantity": 50,
  "categoryId": "550e8400-e29b-41d4-a716-446655440002"
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "클린 코드",
  "description": "로버트 C. 마틴의 클린 코드",
  "price": 33000,
  "count": 50,
  "status": "ACTIVE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "프로그래밍",
  "createdAt": "2026-03-01T09:00:00",
  "images": [
    {
      "imageId": "660e8400-e29b-41d4-a716-446655440001",
      "s3Key": "products/image1.jpg",
      "presignedUrl": null,
      "sortOrder": 0,
      "isThumbnail": true,
      "createdAt": "2026-03-01T09:00:00"
    }
  ]
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
  "path": "/api/products",
  "timestamp": "2026-03-01T09:00:00",
  "errors": [
    {
      "field": "title",
      "rejectedValue": null,
      "message": "상품명은 필수입니다"
    }
  ]
}
```

**3. 클라이언트 오류 — 존재하지 않는 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "카테고리를 찾을 수 없습니다",
  "errorCode": "CATEGORY_NOT_FOUND",
  "path": "/api/products",
  "timestamp": "2026-03-01T09:00:00"
}
```

**4. 권한 오류 — SELLER가 아닌 경우**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "해당 작업을 수행할 권한이 없습니다",
  "errorCode": "UNAUTHORIZED_ROLE",
  "path": "/api/products",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `POST /api/products/check-availability` — 재고 확인 및 차감

주문 시 상품의 재고를 확인하고 차감합니다. Redisson 분산락을 사용하여 동시성을 보장합니다.

#### Request

Request Body:

```json
[
  {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2
  }
]
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productId` | UUID | O | 상품 ID |
| `quantity` | Integer | O | 주문 수량 (최소 1) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
[
  {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "클린 코드",
    "price": 33000,
    "thumbnailKeySnapshot": "products/thumbnail.jpg",
    "productOrderStatus": "ORDERABLE"
  }
]
```

| productOrderStatus | 설명 |
|---|---|
| `ORDERABLE` | 구매 가능 (재고 차감 완료) |
| `INSUFFICIENT_STOCK` | 재고 부족 |
| `NOT_FOR_SALE` | 판매 불가 상품 |

**2. 서버 오류 — 분산락 획득 실패**

Status Code: `503 Service Unavailable`

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "재고 처리 중 경합이 발생했습니다. 잠시 후 다시 시도해주세요",
  "errorCode": "STOCK_LOCK_ACQUISITION_FAILED",
  "path": "/api/products/check-availability",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `PUT /api/products/{productId}` — 상품 수정

상품 정보를 수정합니다. SELLER 권한 필요 (본인 상품만 수정 가능).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 수정할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | String | O | `application/json` |
| `X-User-Id` | String | O | 사용자 ID (Gateway에서 주입) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | String | O | 상품명 |
| `description` | String | X | 상품 설명 |
| `price` | BigDecimal | O | 판매 가격 (0보다 커야 함) |
| `stockQuantity` | Integer | O | 재고 수량 (0 이상) |
| `categoryId` | UUID | O | 카테고리 ID |

Request Body Example:

```json
{
  "title": "클린 코드 개정판",
  "description": "개정된 클린 코드",
  "price": 35000,
  "stockQuantity": 100,
  "categoryId": "550e8400-e29b-41d4-a716-446655440002"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "클린 코드 개정판",
  "description": "개정된 클린 코드",
  "price": 35000,
  "count": 100,
  "status": "ACTIVE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "프로그래밍",
  "createdAt": "2026-03-01T09:00:00",
  "images": []
}
```

**2. 클라이언트 오류 — 존재하지 않는 상품**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "상품을 찾을 수 없습니다",
  "errorCode": "PRODUCT_NOT_FOUND",
  "path": "/api/products/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

**3. 권한 오류 — 본인 상품이 아닌 경우**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "해당 상품에 대한 권한이 없습니다",
  "errorCode": "SELLER_NOT_AUTHORIZED",
  "path": "/api/products/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `DELETE /api/products/{productId}` — 상품 삭제

상품을 소프트 삭제합니다. SELLER 권한 필요 (본인 상품만 삭제 가능).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 삭제할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

**2. 클라이언트 오류 — 이미 삭제된 상품**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "이미 삭제된 상품입니다",
  "errorCode": "PRODUCT_ALREADY_DELETED",
  "path": "/api/products/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `POST /api/products/{productId}/restore` — 상품 복원

소프트 삭제된 상품을 복원합니다. SELLER 권한 필요 (본인 상품만 복원 가능).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 복원할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "클린 코드",
  "description": "로버트 C. 마틴의 클린 코드",
  "price": 33000,
  "count": 50,
  "status": "ACTIVE",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "categoryName": "프로그래밍",
  "createdAt": "2026-03-01T09:00:00",
  "images": []
}
```

---

### `PATCH /api/products/{productId}/stock/increase` — 재고 증가

상품의 재고를 증가시킵니다. SELLER 권한 필요 (본인 상품만).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 상품 ID |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `quantity` | Integer | O | 증가할 수량 (최소 1) |

```json
{
  "quantity": 50
}
```

#### Response

Status Code: `200 OK`

응답 형식은 상품 상세 조회와 동일합니다.

---

### `PATCH /api/products/{productId}/stock/decrease` — 재고 감소

상품의 재고를 감소시킵니다. SELLER 권한 필요 (본인 상품만).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 상품 ID |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `quantity` | Integer | O | 감소할 수량 (최소 1) |

```json
{
  "quantity": 10
}
```

#### Response

Status Code: `200 OK`

응답 형식은 상품 상세 조회와 동일합니다.

---

### `PATCH /api/products/{productId}/status` — 상품 상태 변경

상품의 판매 상태를 변경합니다. SELLER 권한 필요 (본인 상품만).

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | String (UUID) | O | 상품 ID |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | String | O | 변경할 상태 (`ACTIVE` \| `INACTIVE` \| `SOLD_OUT`) |

```json
{
  "status": "INACTIVE"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

응답 형식은 상품 상세 조회와 동일합니다.

---

## 📌 Category API

| 기능 | Method | Endpoint | 구현 | 설명 |
|------|--------|----------|:---:|------|
| 카테고리 목록 조회 | GET | /api/categories | ✅ | 전체 또는 depth별 조회 |
| 카테고리 상세 조회 | GET | /api/categories/{categoryId} | ✅ | 단일 카테고리 조회 |
| 하위 카테고리 조회 | GET | /api/categories/{categoryId}/children | ✅ | 자식 카테고리 목록 |
| 카테고리 추가 (관리자) | POST | /api/categories/admin | ✅ | ADMIN 전용 |
| 카테고리 추가 (판매자) | POST | /api/categories | ✅ | SELLER 전용 (하위 카테고리만) |
| 카테고리 수정 (관리자) | PUT | /api/categories/admin/{categoryId} | ✅ | ADMIN 전용 |
| 카테고리 수정 (판매자) | PUT | /api/categories/{categoryId} | ✅ | SELLER 전용 (본인 카테고리만) |
| 카테고리 삭제 | DELETE | /api/categories/{categoryId} | ✅ | 하위 카테고리 없을 때만 |

---

### `GET /api/categories` — 카테고리 목록 조회

카테고리 목록을 조회합니다. depth 파라미터로 특정 깊이만 필터링할 수 있습니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `depth` | Integer | X | 카테고리 깊이 (0=대분류, 1=중분류, 2=소분류) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
[
  {
    "categoryId": "550e8400-e29b-41d4-a716-446655440010",
    "sellerId": null,
    "name": "국내도서",
    "description": "국내 도서 카테고리",
    "depth": 0,
    "sortOrder": 1,
    "parentId": null,
    "createdAt": "2026-03-01T09:00:00"
  }
]
```

---

### `GET /api/categories/{categoryId}` — 카테고리 상세 조회

특정 카테고리의 상세 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | UUID | O | 카테고리 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440010",
  "sellerId": null,
  "name": "국내도서",
  "description": "국내 도서 카테고리",
  "depth": 0,
  "sortOrder": 1,
  "parentId": null,
  "createdAt": "2026-03-01T09:00:00"
}
```

**2. 클라이언트 오류 — 존재하지 않는 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "카테고리를 찾을 수 없습니다",
  "errorCode": "CATEGORY_NOT_FOUND",
  "path": "/api/categories/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `GET /api/categories/{categoryId}/children` — 하위 카테고리 조회

특정 카테고리의 자식 카테고리 목록을 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | UUID | O | 부모 카테고리 ID |

#### Response

Status Code: `200 OK`

응답 형식은 카테고리 목록 조회와 동일합니다.

---

### `POST /api/categories/admin` — 카테고리 추가 (관리자)

관리자가 카테고리를 추가합니다. 대분류(depth 0) 생성 가능.

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 카테고리명 (최대 50자) |
| `description` | String | X | 카테고리 설명 (최대 500자) |
| `sortOrder` | Integer | O | 정렬 순서 |
| `parentId` | UUID | X | 부모 카테고리 ID (미입력 시 대분류) |

```json
{
  "name": "컴퓨터/IT",
  "description": "IT 관련 도서",
  "sortOrder": 1,
  "parentId": null
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440020",
  "sellerId": null,
  "name": "컴퓨터/IT",
  "description": "IT 관련 도서",
  "depth": 0,
  "sortOrder": 1,
  "parentId": null,
  "createdAt": "2026-03-01T09:00:00"
}
```

**2. 클라이언트 오류 — 소분류 이하 생성 불가**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "소분류 이하로는 카테고리를 생성할 수 없습니다",
  "errorCode": "CATEGORY_DEPTH_EXCEEDED",
  "path": "/api/categories/admin",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `POST /api/categories` — 카테고리 추가 (판매자)

판매자가 하위 카테고리를 추가합니다. 대분류(depth 0) 생성 불가.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 판매자 ID (Gateway에서 주입) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 카테고리명 (최대 50자) |
| `description` | String | X | 카테고리 설명 |
| `sortOrder` | Integer | O | 정렬 순서 |
| `parentId` | UUID | O | 부모 카테고리 ID (필수) |

#### Response

**1. 요청 성공**

Status Code: `201 Created`

응답 형식은 카테고리 추가 (관리자)와 동일합니다. (`sellerId` 필드에 판매자 ID 포함)

**2. 권한 오류 — 대분류 생성 시도**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "판매자는 대분류를 생성할 수 없습니다",
  "errorCode": "SELLER_CANNOT_CREATE_ROOT_CATEGORY",
  "path": "/api/categories",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

### `PUT /api/categories/admin/{categoryId}` — 카테고리 수정 (관리자)

관리자가 카테고리 정보를 수정합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | UUID | O | 수정할 카테고리 ID |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 카테고리명 |
| `description` | String | X | 카테고리 설명 |
| `sortOrder` | Integer | O | 정렬 순서 |

#### Response

Status Code: `200 OK`

응답 형식은 카테고리 상세 조회와 동일합니다.

---

### `PUT /api/categories/{categoryId}` — 카테고리 수정 (판매자)

판매자가 본인이 생성한 카테고리를 수정합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | UUID | O | 수정할 카테고리 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-User-Id` | String | O | 판매자 ID |

Request Body: 카테고리 수정 (관리자)와 동일

#### Response

Status Code: `200 OK`

응답 형식은 카테고리 상세 조회와 동일합니다.

---

### `DELETE /api/categories/{categoryId}` — 카테고리 삭제

카테고리를 소프트 삭제합니다. 하위 카테고리가 존재하면 삭제할 수 없습니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | UUID | O | 삭제할 카테고리 ID |

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

**2. 클라이언트 오류 — 하위 카테고리 존재**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "하위 카테고리가 존재하여 삭제할 수 없습니다",
  "errorCode": "CATEGORY_HAS_CHILDREN",
  "path": "/api/categories/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

**3. 클라이언트 오류 — 이미 삭제된 카테고리**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "이미 삭제된 카테고리입니다",
  "errorCode": "CATEGORY_ALREADY_DELETED",
  "path": "/api/categories/550e8400-...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

## 📌 Product Image API

| 기능 | Method | Endpoint | 구현 | 설명 |
|------|--------|----------|:---:|------|
| 이미지 업로드 | POST | /api/products/{productId}/images | ✅ | 상품 이미지 추가 |
| 이미지 삭제 | DELETE | /api/products/{productId}/images/{imageId} | ✅ | 상품 이미지 삭제 |

---

### `POST /api/products/{productId}/images` — 이미지 업로드

상품에 이미지를 업로드합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 상품 ID |

Request Parts:

| 파트 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `file` | MultipartFile | O | 이미지 파일 |
| `sortOrder` | Integer | X | 정렬 순서 (기본값: 0) |
| `isThumbnail` | Boolean | X | 썸네일 여부 (기본값: false) |

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "imageId": "660e8400-e29b-41d4-a716-446655440001",
  "s3Key": "products/image1.jpg",
  "presignedUrl": null,
  "sortOrder": 0,
  "isThumbnail": true,
  "createdAt": "2026-03-01T09:00:00"
}
```

---

### `DELETE /api/products/{productId}/images/{imageId}` — 이미지 삭제

상품의 특정 이미지를 삭제합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 상품 ID |
| `imageId` | UUID | O | 이미지 ID |

#### Response

**1. 요청 성공**

Status Code: `204 No Content`

**2. 클라이언트 오류 — 이미지를 찾을 수 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "이미지를 찾을 수 없습니다",
  "errorCode": "PRODUCT_IMAGE_NOT_FOUND",
  "path": "/api/products/.../images/...",
  "timestamp": "2026-03-01T09:00:00"
}
```

**3. 권한 오류 — 해당 상품의 이미지가 아님**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "해당 상품의 이미지가 아닙니다",
  "errorCode": "IMAGE_NOT_OWNED_BY_PRODUCT",
  "path": "/api/products/.../images/...",
  "timestamp": "2026-03-01T09:00:00"
}
```

---

## 📌 Enum 정의

### ProductStatus

| 값 | 설명 |
|----|------|
| `ACTIVE` | 판매중 |
| `INACTIVE` | 판매중지 |
| `SOLD_OUT` | 품절 |

### ProductOrderStatus

| 값 | 설명 |
|----|------|
| `ORDERABLE` | 구매 가능 |
| `INSUFFICIENT_STOCK` | 재고 부족 |
| `NOT_FOR_SALE` | 판매 불가 상품 |

---

## 📌 공통 에러 코드

| ErrorCode | HTTP Status | 메시지 |
|-----------|-------------|--------|
| `INVALID_INPUT_VALUE` | 400 | 입력값이 올바르지 않습니다 |
| `INVALID_PRICE` | 400 | 가격은 0보다 커야 합니다 |
| `CATEGORY_DEPTH_EXCEEDED` | 400 | 소분류 이하로는 카테고리를 생성할 수 없습니다 |
| `CATEGORY_HAS_CHILDREN` | 400 | 하위 카테고리가 존재하여 삭제할 수 없습니다 |
| `SELLER_NOT_AUTHORIZED` | 403 | 해당 상품에 대한 권한이 없습니다 |
| `UNAUTHORIZED_ROLE` | 403 | 해당 작업을 수행할 권한이 없습니다 |
| `SELLER_CANNOT_CREATE_ROOT_CATEGORY` | 403 | 판매자는 대분류를 생성할 수 없습니다 |
| `IMAGE_NOT_OWNED_BY_PRODUCT` | 403 | 해당 상품의 이미지가 아닙니다 |
| `PRODUCT_NOT_FOUND` | 404 | 상품을 찾을 수 없습니다 |
| `CATEGORY_NOT_FOUND` | 404 | 카테고리를 찾을 수 없습니다 |
| `PRODUCT_IMAGE_NOT_FOUND` | 404 | 이미지를 찾을 수 없습니다 |
| `PRODUCT_ALREADY_DELETED` | 409 | 이미 삭제된 상품입니다 |
| `CATEGORY_ALREADY_DELETED` | 409 | 이미 삭제된 카테고리입니다 |
| `STOCK_LOCK_ACQUISITION_FAILED` | 503 | 재고 처리 중 경합이 발생했습니다. 잠시 후 다시 시도해주세요 |
