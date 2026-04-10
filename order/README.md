# Order Module

주문, 배송 도메인을 담당하는 마이크로서비스입니다.  
헥사고날 아키텍처를 기반으로 설계되었으며, Kafka를 통한 비동기 결제 처리 및 외부 배송 추적 API를 연동합니다.

---

## 목차

- [아키텍처](#아키텍처)
- [도메인 모델](#도메인-모델)
- [Enum 정의](#enum-정의)
- [API 명세](#api-명세)
  - [주문 API](#주문-api)
  - [배송 API](#배송-api)
- [에러 코드](#에러-코드)
- [외부 연동](#외부-연동)
- [이벤트](#이벤트)

---

## 아키텍처

```
order/
├── presentation/         # REST Controller, Request/Response DTO
├── application/          # UseCase 인터페이스, Service, Port 인터페이스
├── domain/               # 엔티티, 도메인 로직, Repository 인터페이스, Enum
├── infrastructure/       # Feign Client, Kafka, JPA Repository 구현체
├── common/               # 공통 예외 처리
└── config/               # Spring 설정
```

헥사고날 아키텍처(Ports & Adapters) 패턴을 적용합니다.

- **Port**: `ProductPort`, `PaymentPort`, `TrackingPort` (인터페이스)
- **Adapter**: `ProductClientAdapter`, `PaymentClientAdapter`, `SweetTrackerClientAdapter` (구현체)
- **UseCase**: `OrderCreateUseCase`, `OrderSearchUseCase`, `DeliveryTrackingUseCase`

---

## 도메인 모델

### Order (주문)

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | UUID | 주문 ID (PK) |
| buyerId | UUID | 구매자 ID |
| totalPrice | BigDecimal | 총 결제 금액 |
| status | OrderStatus | 주문 상태 |
| address | String | 배송지 주소 |
| addressDetail | String | 상세 주소 |
| zipCode | String | 우편번호 |
| receiver | String | 수령인 이름 |
| receiverPhone | String | 수령인 연락처 |
| representativeProductName | String | 대표 상품명 (첫 번째 상품) |
| representativeThumbnailKey | String | 대표 상품 썸네일 키 |
| itemCount | Integer | 주문 상품 수 |
| createdAt | LocalDateTime | 주문 생성 시각 |
| updatedAt | LocalDateTime | 주문 수정 시각 |

### OrderItem (주문 상품)

| 필드 | 타입 | 설명 |
|------|------|------|
| orderItemId | UUID | 주문 상품 ID (PK) |
| orderId | UUID | 주문 ID (FK) |
| productId | UUID | 상품 ID (FK) |
| sellerId | UUID | 판매자 ID |
| productNameSnapshot | String | 주문 시점 상품명 스냅샷 |
| unitPriceSnapshot | BigDecimal | 주문 시점 단가 스냅샷 |
| thumbnailKeySnapshot | String | 주문 시점 썸네일 키 스냅샷 |
| quantity | Integer | 수량 |
| status | OrderItemStatus | 주문 상품 상태 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

> 주문 시점의 상품 정보를 스냅샷으로 저장하여, 이후 상품 정보가 변경되어도 주문 내역이 보존됩니다.

### Delivery (배송)

| 필드 | 타입 | 설명 |
|------|------|------|
| deliveryId | UUID | 배송 ID (PK) |
| orderItemId | UUID | 주문 상품 ID (FK) |
| sellerId | UUID | 판매자 ID |
| buyerId | UUID | 구매자 ID |
| courierCode | String | 택배사 코드 |
| invoiceNumber | String | 운송장 번호 |
| status | DeliveryStatus | 배송 상태 |
| shippedAt | LocalDateTime | 출고 시각 |
| deliveredAt | LocalDateTime | 배송 완료 시각 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

---

## Enum 정의

### OrderStatus (주문 상태)

| 값 | 설명 |
|----|------|
| `CREATED` | 주문 생성 완료, 결제 대기 중 |
| `CONFIRMED` | 결제 완료 |
| `SHIPPING` | 전체 상품 배송 중 |
| `PARTIAL_SHIPPING` | 일부 상품 배송 중 |
| `COMPLETED` | 전체 상품 배송 완료 |
| `PARTIAL_CANCELED` | 일부 상품 취소 |
| `CANCELED` | 전체 주문 취소 |

### OrderItemStatus (주문 상품 상태)

| 값 | 설명 |
|----|------|
| `PENDING` | 주문 접수 |
| `PREPARING` | 상품 준비 중 |
| `SHIPPING` | 배송 중 |
| `DELIVERED` | 배송 완료 |
| `CANCELLED` | 취소됨 |

### DeliveryStatus (배송 상태)

| 값 | 설명 |
|----|------|
| `PREPARING` | 출고 준비 중 |
| `SHIPPED` | 배송 중 |
| `DELIVERED` | 배송 완료 |

---

## API 명세

- **Base URL**: `/api`
- 모든 API는 인증된 사용자만 접근 가능합니다.
- 인증된 사용자의 ID는 `@CurrentMember` 어노테이션으로 주입됩니다.

---

## 주문 API

### 주문 생성

**POST** `/api/orders`

주문을 생성하고, 결제를 처리합니다.

**Request Body**

```json
{
  "address": "서울특별시 강남구 테헤란로",
  "addressDetail": "123호",
  "zipCode": "06234",
  "receiver": "홍길동",
  "receiverPhone": "010-1234-5678",
  "orderItemRequest": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    },
    {
      "productId": "550e8400-e29b-41d4-a716-446655440001",
      "quantity": 1
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| address | String | Y | 배송지 주소 |
| addressDetail | String | Y | 상세 주소 |
| zipCode | String | Y | 우편번호 |
| receiver | String | Y | 수령인 이름 |
| receiverPhone | String | Y | 수령인 연락처 |
| orderItemRequest | List | Y | 주문 상품 목록 (1개 이상) |
| orderItemRequest[].productId | UUID | Y | 상품 ID |
| orderItemRequest[].quantity | Integer | Y | 수량 (최소 1) |

**Response** `201 Created`

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440010",
  "totalPrice": 35000,
  "status": "CONFIRMED",
  "createdAt": "2024-01-15T10:30:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | UUID | 생성된 주문 ID |
| totalPrice | BigDecimal | 총 결제 금액 |
| status | OrderStatus | 주문 상태 |
| createdAt | LocalDateTime | 주문 생성 시각 |

**주문 생성 흐름**

```
1. 중복 상품 요청 검증
2. Product Service → 상품 존재 여부 / 재고 / 판매 상태 확인
3. Order 및 OrderItem 생성, DB 저장
4. Payment Service → 결제 처리 (동기 호출)
5. 결제 금액 및 상태 검증
6. 주문 상태 CONFIRMED 업데이트
7. Delivery 레코드 자동 생성
8. Kafka → order.created 이벤트 발행
```

---

### 주문 목록 조회

**GET** `/api/orders`

인증된 사용자의 주문 목록을 페이지네이션으로 조회합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | Integer | N | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | N | 10 | 페이지 크기 |
| sort | String | N | createdAt,desc | 정렬 기준 |

**Response** `200 OK`

```json
{
  "content": [
    {
      "orderId": "550e8400-e29b-41d4-a716-446655440010",
      "totalPrice": 35000,
      "status": "CONFIRMED",
      "createdAt": "2024-01-15T10:30:00",
      "representativeProductName": "오늘의 점심 도시락",
      "representativeThumbnailKey": "images/thumbnail/abc123.jpg",
      "itemCount": 2
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "number": 0,
  "size": 10
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | UUID | 주문 ID |
| totalPrice | BigDecimal | 총 결제 금액 |
| status | OrderStatus | 주문 상태 |
| createdAt | LocalDateTime | 주문 생성 시각 |
| representativeProductName | String | 대표 상품명 |
| representativeThumbnailKey | String | 대표 상품 썸네일 키 |
| itemCount | Integer | 주문 상품 수 |

---

### 주문 상세 조회

**GET** `/api/orders/{orderId}`

특정 주문의 상세 정보와 주문 상품 목록을 조회합니다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| orderId | UUID | 주문 ID |

**Response** `200 OK`

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440010",
  "totalPrice": 35000,
  "createdAt": "2024-01-15T10:30:00",
  "address": "서울특별시 강남구 테헤란로",
  "addressDetail": "123호",
  "zipCode": "06234",
  "receiver": "홍길동",
  "receiverPhone": "010-1234-5678",
  "itemCount": 2,
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "productName": "오늘의 점심 도시락",
      "unitPrice": 15000,
      "quantity": 2,
      "status": "CONFIRMED",
      "thumbnailKey": "images/thumbnail/abc123.jpg"
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | UUID | 주문 ID |
| totalPrice | BigDecimal | 총 결제 금액 |
| createdAt | LocalDateTime | 주문 생성 시각 |
| address | String | 배송지 주소 |
| addressDetail | String | 상세 주소 |
| zipCode | String | 우편번호 |
| receiver | String | 수령인 이름 |
| receiverPhone | String | 수령인 연락처 |
| itemCount | Integer | 주문 상품 수 |
| items[].productId | UUID | 상품 ID |
| items[].productName | String | 주문 시점 상품명 |
| items[].unitPrice | BigDecimal | 주문 시점 단가 |
| items[].quantity | Integer | 수량 |
| items[].status | OrderItemStatus | 주문 상품 상태 |
| items[].thumbnailKey | String | 썸네일 키 |

---

### 주문 취소 (예정)

**DELETE** `/api/orders/{orderId}`

주문을 취소합니다. `CONFIRMED` 상태인 경우에만 취소가 가능합니다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| orderId | UUID | 주문 ID |

**Response** `200 OK`

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440010",
  "status": "CANCELED"
}
```

---

## 배송 API

### 배송 추적 조회

**GET** `/api/deliveries/{deliveryId}/tracking`

SweetTracker API를 통해 실시간 배송 추적 정보를 조회합니다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| deliveryId | UUID | 배송 ID |

**Response** `200 OK`

```json
{
  "courierCode": "04",
  "invoiceNumber": "123456789012",
  "delivered": false,
  "details": [
    {
      "time": "2024-01-16 14:30:00",
      "location": "서울 강남 배송센터",
      "status": "배송출발"
    },
    {
      "time": "2024-01-16 09:00:00",
      "location": "경기 성남 Hub",
      "status": "Hub 도착"
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| courierCode | String | 택배사 코드 (예: "04" = CJ대한통운) |
| invoiceNumber | String | 운송장 번호 |
| delivered | Boolean | 배송 완료 여부 |
| details[].time | String | 배송 이력 시각 |
| details[].location | String | 배송 이력 위치 |
| details[].status | String | 배송 이력 상태 |

---

### 배송 목록 조회 (예정)

**GET** `/api/deliveries`

인증된 사용자의 배송 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | Integer | N | 0 | 페이지 번호 |
| size | Integer | N | 10 | 페이지 크기 |

**Response** `200 OK`

```json
{
  "content": [
    {
      "deliveryId": "550e8400-e29b-41d4-a716-446655440020",
      "orderItemId": "550e8400-e29b-41d4-a716-446655440011",
      "productName": "오늘의 점심 도시락",
      "status": "SHIPPED",
      "courierCode": "04",
      "invoiceNumber": "123456789012",
      "shippedAt": "2024-01-16T09:00:00",
      "deliveredAt": null
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

## 에러 코드

| HTTP Status | 에러 코드 | 설명 |
|-------------|-----------|------|
| 400 | `INVALID_INPUT_VALUE` | 요청 값이 유효하지 않음 |
| 400 | `DUPLICATE_PRODUCT_REQUEST` | 동일한 상품이 중복 요청됨 |
| 400 | `PRODUCT_NOT_ORDERABLE` | 판매 중이지 않은 상품 |
| 400 | `INSUFFICIENT_STOCK` | 상품 재고 부족 |
| 400 | `INVALID_PAYMENT_STATUS` | 유효하지 않은 결제 상태 |
| 404 | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없음 |
| 404 | `ORDER_NOT_FOUND` | 주문을 찾을 수 없음 |
| 404 | `DELIVERY_NOT_FOUND` | 배송 정보를 찾을 수 없음 |
| 409 | `INVALID_PAYMENT_AMOUNT` | 결제 금액 불일치 |
| 409 | `PAYMENT_FAILED` | 결제 처리 실패 |
| 502 | `EXTERNAL_SERVICE_ERROR` | 외부 서비스 연동 실패 |

**Error Response 형식**

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "status": 404
}
```

---

## 외부 연동

### Product Service

주문 생성 시 상품 가용성을 검증합니다.

- **Endpoint**: `POST /api/products/check-availability`
- **Host**: `http://localhost:8081`
- **요청**: 상품 ID와 수량 목록
- **응답**: 상품 상세 정보 및 주문 가능 상태

| ProductOrderStatus | 설명 |
|-------------------|------|
| `ORDERABLE` | 주문 가능 |
| `INSUFFICIENT_STOCK` | 재고 부족 |
| `NOT_FOR_SALE` | 판매 불가 (판매 중단 등) |

### Payment Service

주문 생성 시 결제를 동기적으로 처리합니다.

- **Endpoint**: `POST /api/payments/orders`
- **Host**: `http://localhost:8082`
- **요청**: 주문 및 주문 상품 정보
- **응답**: 결제 상태(SUCCESS/FAILED) 및 결제 금액

### SweetTracker (배송 추적)

실시간 배송 추적 정보를 제공합니다.

- **Base URL**: `https://info.sweettracker.co.kr`
- **Endpoint**: `GET /api/v1/trackingInfo`
- **파라미터**: `t_key` (API 키), `t_code` (택배사 코드), `t_invoice` (운송장 번호)

---

## 이벤트

### 발행 (Producer)

| 토픽 | 이벤트 | 발행 시점 |
|------|--------|-----------|
| `order.created` | `OrderCreatedEvent` | 주문 확정(CONFIRMED) 후 |

**OrderCreatedEvent**

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440010",
  "buyerId": "550e8400-e29b-41d4-a716-446655440001",
  "totalPrice": 35000,
  "items": [
    {
      "orderItemId": "...",
      "productId": "...",
      "sellerId": "...",
      "quantity": 2,
      "unitPrice": 15000
    }
  ]
}
```

### 구독 (Consumer)

| 토픽 | 이벤트 | 처리 내용 |
|------|--------|-----------|
| `payment.order-payment-result` | `PaymentResultEvent` | 결제 결과 수신 후 주문 상태 업데이트 및 배송 생성 |

- **Consumer Group**: `order-group`
- 결제 성공 시: 금액 검증 → 주문 확정(CONFIRMED) → Delivery 레코드 생성
- 결제 실패 시: 별도 처리 없이 로그 기록
