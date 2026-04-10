# Gateway API Spec

## 개요
- 모듈: `gateway-service`
- 역할: 라우팅, JWT 검증, Swagger 집계
- 자체 비즈니스 API 보다는 진입점과 정책 문서의 성격이 강하다.

## 공개 경로
```json
[
  "/api/auth",
  "/api/auth/login",
  "/api/auth/refresh",
  "/api/auth/profile-images/presign",
  "/swagger/**",
  "/swagger-ui.html"
]
```

## 인증 성공 시 전달 헤더
```json
{
  "X-Member-Id": "11111111-1111-1111-1111-111111111111",
  "X-Member-Role": "USER"
}
```

## 라우팅 목록
| Route ID | Path | Target | 구현 여부 |
| --- | --- | --- | --- |
| `member-auth-service` | `/api/auth/**` | `lb://member-service` | 완료 |
| `member-service-v1` | `/api/members/**` | `lb://member-service` | 완료 |
| `member-seller-service` | `/api/sellers/**` | `lb://member-service` | 완료 |
| `member-report-service` | `/api/member-reports/**` | `lb://member-service` | 완료 |
| `member-admin-report-service` | `/api/admin/member-reports/**` | `lb://member-service` | 완료 |
| `member-admin-restriction-service` | `/api/admin/member-restrictions/**` | `lb://member-service` | 완료 |
| `product-service` | `/api/product/**` | `lb://product-service` | 완료 |
| `category-service` | `/api/category/**`, `/api/categories/**` | `lb://product-service` | 완료 |
| `cart-service` | `/api/carts/**` | `lb://cart-service` | 완료 |
| `wish-service` | `/api/wishes/**` | `lb://cart-service` | 완료 |
| `order-service` | `/api/orders/**` | `lb://order-service` | 완료 |
| `payment-service` | `/api/payments/**` | `lb://payment-service` | 완료 |
| `settlement-service` | `/api/settlements/**` | `lb://settlement-service` | 완료 |
| `notification-service` | `/api/notifications/**` | `lb://notification-service` | 완료 |

## 요청 예시
### 보호된 API 호출
```http
GET /api/members/me HTTP/1.1
Host: gateway.local
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
```

### 게이트웨이 내부에서 다운스트림으로 전달되는 형태
```http
GET /api/members/me HTTP/1.1
Host: member-service
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.access-token
X-Member-Id: 11111111-1111-1111-1111-111111111111
X-Member-Role: USER
```

## 인증 실패 응답 예시
```http
HTTP/1.1 401 Unauthorized
```

## Swagger 집계 경로
```json
[
  {
    "name": "member-service",
    "url": "/swagger/member/v3/api-docs"
  },
  {
    "name": "payment-service",
    "url": "/swagger/payment/v3/api-docs"
  },
  {
    "name": "settlement-service",
    "url": "/swagger/settlement/v3/api-docs"
  },
  {
    "name": "product-service",
    "url": "/swagger/product/v3/api-docs"
  },
  {
    "name": "cart-service",
    "url": "/swagger/cart/v3/api-docs"
  },
  {
    "name": "order-service",
    "url": "/swagger/order/v3/api-docs"
  },
  {
    "name": "notification-service",
    "url": "/swagger/notification/v3/api-docs"
  }
]
```
