# Seller API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/sellers/register` | 판매자 등록 | Header: 인증 정보, Body: `bankName`, `account` | 완료 |
| `GET` | `/api/sellers/me` | 내 판매자 정보 조회 | Header: 인증 정보 | 완료 |

## 1. 판매자 등록
### `POST /api/sellers/register`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |
| Body | `bankName` | String | Y |
| Body | `account` | String | Y |

#### Request JSON
```json
{
  "bankName": "카카오뱅크",
  "account": "3333-12-1234567"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "sellerId": "55555555-5555-5555-5555-555555555555",
    "memberId": "11111111-1111-1111-1111-111111111111",
    "bankName": "카카오뱅크",
    "account": "3333-12-1234567",
    "approvedAt": "2026-04-10T12:00:00"
  },
  "error": null
}
```

## 2. 내 판매자 정보 조회
### `GET /api/sellers/me`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |

#### Response JSON
```json
{
  "success": true,
  "data": {
    "sellerId": "55555555-5555-5555-5555-555555555555",
    "memberId": "11111111-1111-1111-1111-111111111111",
    "bankName": "카카오뱅크",
    "account": "3333-12-1234567",
    "approvedAt": "2026-04-10T12:00:00"
  },
  "error": null
}
```
