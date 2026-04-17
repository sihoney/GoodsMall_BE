# Member API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/members/me` | 내 회원 정보 조회 | Header: 인증 정보 | 완료 |
| `PATCH` | `/api/members/me` | 내 회원 정보 수정 | Header: 인증 정보, Body: `email`, `password`, `nickname`, `phone`, `address`, `profileImageKey` | 완료 |
| `GET` | `/api/members/{memberId}` | 회원 단건 조회 | Path: `memberId` | 완료 |
| `PATCH` | `/api/members/{memberId}` | 회원 단건 수정 | Path: `memberId`, Body: `email`, `password`, `nickname`, `phone`, `address`, `profileImageKey` | 완료 |

## 1. 내 회원 정보 조회
### `GET /api/members/me`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |

#### Response JSON
```json
{
  "success": true,
  "data": {
    "memberId": "11111111-1111-1111-1111-111111111111",
    "email": "user@test.local",
    "nickname": "점심유저",
    "phone": "010-1234-5678",
    "address": "서울시 강남구",
    "profileImageUrl": "https://cdn.example.com/profiles/2026/04/user.png",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2026-04-10T11:00:00",
    "updatedAt": "2026-04-10T11:20:00"
  },
  "error": null
}
```

## 2. 내 회원 정보 수정
### `PATCH /api/members/me`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |
| Body | `email` | String | Y |
| Body | `password` | String | Y |
| Body | `nickname` | String | Y |
| Body | `phone` | String | N |
| Body | `address` | String | N |
| Body | `profileImageKey` | String | N |

#### Request JSON
```json
{
  "email": "user@test.local",
  "password": "newPassword123!",
  "nickname": "점심유저2",
  "phone": "010-9999-0000",
  "address": "서울시 서초구",
  "profileImageKey": "profiles/2026/04/user-new.png"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "memberId": "11111111-1111-1111-1111-111111111111",
    "email": "user@test.local",
    "nickname": "점심유저2",
    "phone": "010-9999-0000",
    "address": "서울시 서초구",
    "profileImageUrl": "https://cdn.example.com/profiles/2026/04/user-new.png",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2026-04-10T11:00:00",
    "updatedAt": "2026-04-10T11:25:00"
  },
  "error": null
}
```

## 3. 회원 단건 조회
### `GET /api/members/{memberId}`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Path | `memberId` | UUID | Y |

#### Response JSON
```json
{
  "memberId": "11111111-1111-1111-1111-111111111111",
  "email": "user@test.local",
  "nickname": "점심유저",
  "phone": "010-1234-5678",
  "address": "서울시 강남구",
  "profileImageUrl": "https://cdn.example.com/profiles/2026/04/user.png",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-04-10T11:00:00",
  "updatedAt": "2026-04-10T11:20:00"
}
```

## 4. 회원 단건 수정
### `PATCH /api/members/{memberId}`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Path | `memberId` | UUID | Y |
| Body | `email` | String | Y |
| Body | `password` | String | Y |
| Body | `nickname` | String | Y |
| Body | `phone` | String | N |
| Body | `address` | String | N |
| Body | `profileImageKey` | String | N |

#### Request JSON
```json
{
  "email": "user@test.local",
  "password": "adminChanged123!",
  "nickname": "운영수정닉네임",
  "phone": "010-0000-0000",
  "address": "서울시 중구",
  "profileImageKey": null
}
```

#### Response JSON
```json
{
  "memberId": "11111111-1111-1111-1111-111111111111",
  "email": "user@test.local",
  "nickname": "운영수정닉네임",
  "phone": "010-0000-0000",
  "address": "서울시 중구",
  "profileImageUrl": null,
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-04-10T11:00:00",
  "updatedAt": "2026-04-10T11:30:00"
}
```
