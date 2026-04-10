# Auth API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth` | 회원 가입 | Body: `email`, `password`, `nickname`, `phone`, `address`, `profileImageKey`, `role` | 완료 |
| `POST` | `/api/auth/login` | 로그인 | Body: `email`, `password` | 완료 |
| `POST` | `/api/auth/refresh` | 액세스 토큰 재발급 | Body: `refreshToken` | 완료 |
| `POST` | `/api/auth/logout/{memberId}` | 로그아웃 | Path: `memberId` | 완료 |

## 공통 응답 형식
```json
{
  "success": true,
  "data": {},
  "error": null
}
```

## 1. 회원 가입
### `POST /api/auth`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Body | `email` | String | Y |
| Body | `password` | String | Y |
| Body | `nickname` | String | Y |
| Body | `phone` | String | N |
| Body | `address` | String | N |
| Body | `profileImageKey` | String | N |
| Body | `role` | String | N |

#### Request JSON
```json
{
  "email": "user@test.local",
  "password": "password123!",
  "nickname": "점심유저",
  "phone": "010-1234-5678",
  "address": "서울시 강남구",
  "profileImageKey": "profiles/2026/04/user.png",
  "role": "USER"
}
```

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
    "updatedAt": "2026-04-10T11:00:00"
  },
  "error": null
}
```

## 2. 로그인
### `POST /api/auth/login`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Body | `email` | String | Y |
| Body | `password` | String | Y |

#### Request JSON
```json
{
  "email": "user@test.local",
  "password": "password123!"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.access-token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 3600000,
    "refreshTokenExpiresIn": 1209600000
  },
  "error": null
}
```

## 3. 토큰 재발급
### `POST /api/auth/refresh`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Body | `refreshToken` | String | Y |

#### Request JSON
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.new-access-token",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 3600000,
    "refreshTokenExpiresIn": 1209600000
  },
  "error": null
}
```

## 4. 로그아웃
### `POST /api/auth/logout/{memberId}`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Path | `memberId` | UUID | Y |

#### Request Example
```text
POST /api/auth/logout/11111111-1111-1111-1111-111111111111
```

#### Response JSON
```json
{
  "success": true,
  "data": null,
  "error": null
}
```
