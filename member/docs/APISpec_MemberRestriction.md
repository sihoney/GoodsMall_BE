# MemberRestriction API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/member-restrictions` | 회원 제재 생성 | Header: 관리자 인증 정보, Body: `memberId`, `reason`, `restrictionType`, `durationHours` | 완료 |
| `PATCH` | `/api/admin/member-restrictions/{restrictionId}/deactivate` | 제재 비활성화 | Header: 관리자 인증 정보, Path: `restrictionId` | 완료 |
| `GET` | `/api/admin/member-restrictions/members/{memberId}` | 회원 제재 목록 조회 | Header: 관리자 인증 정보, Path: `memberId` | 완료 |

## 1. 회원 제재 생성
### `POST /api/admin/member-restrictions`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Body | `memberId` | UUID | Y |
| Body | `reason` | String | Y |
| Body | `restrictionType` | Enum | Y |
| Body | `durationHours` | Integer | Y |

#### Request JSON
```json
{
  "memberId": "22222222-2222-2222-2222-222222222222",
  "reason": "운영 정책 위반",
  "restrictionType": "LOGIN_BAN",
  "durationHours": 24
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "restrictionId": "44444444-4444-4444-4444-444444444444",
    "memberId": "22222222-2222-2222-2222-222222222222",
    "adminId": "99999999-9999-9999-9999-999999999999",
    "reason": "운영 정책 위반",
    "restrictionType": "LOGIN_BAN",
    "durationHours": 24,
    "endAt": "2026-04-11T11:55:00",
    "active": true,
    "createdAt": "2026-04-10T11:55:00",
    "updatedAt": "2026-04-10T11:55:00"
  },
  "error": null
}
```

## 2. 제재 비활성화
### `PATCH /api/admin/member-restrictions/{restrictionId}/deactivate`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Path | `restrictionId` | UUID | Y |

#### Response JSON
```json
{
  "success": true,
  "data": {
    "restrictionId": "44444444-4444-4444-4444-444444444444",
    "memberId": "22222222-2222-2222-2222-222222222222",
    "adminId": "99999999-9999-9999-9999-999999999999",
    "reason": "운영 정책 위반",
    "restrictionType": "LOGIN_BAN",
    "durationHours": 24,
    "endAt": "2026-04-11T11:55:00",
    "active": false,
    "createdAt": "2026-04-10T11:55:00",
    "updatedAt": "2026-04-10T12:05:00"
  },
  "error": null
}
```

## 3. 회원 제재 목록 조회
### `GET /api/admin/member-restrictions/members/{memberId}`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Path | `memberId` | UUID | Y |

#### Response JSON
```json
{
  "success": true,
  "data": [
    {
      "restrictionId": "44444444-4444-4444-4444-444444444444",
      "memberId": "22222222-2222-2222-2222-222222222222",
      "adminId": "99999999-9999-9999-9999-999999999999",
      "reason": "운영 정책 위반",
      "restrictionType": "LOGIN_BAN",
      "durationHours": 24,
      "endAt": "2026-04-11T11:55:00",
      "active": true,
      "createdAt": "2026-04-10T11:55:00",
      "updatedAt": "2026-04-10T11:55:00"
    }
  ],
  "error": null
}
```
