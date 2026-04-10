# MemberReport API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/member-reports` | 회원 신고 생성 | Header: 인증 정보, Body: `reportedMemberId`, `reason`, `reportType` | 완료 |
| `GET` | `/api/member-reports/me` | 내 신고 목록 조회 | Header: 인증 정보 | 완료 |
| `GET` | `/api/admin/member-reports` | 전체 신고 목록 조회 | Header: 관리자 인증 정보 | 완료 |
| `GET` | `/api/admin/member-reports/{reportId}` | 신고 상세 조회 | Header: 관리자 인증 정보, Path: `reportId` | 완료 |
| `GET` | `/api/admin/member-reports/members/{memberId}` | 특정 회원 대상 신고 목록 조회 | Header: 관리자 인증 정보, Path: `memberId` | 완료 |
| `PATCH` | `/api/admin/member-reports/{reportId}/approve` | 신고 승인 | Header: 관리자 인증 정보, Path: `reportId`, Body: `reviewComment`, `restrictionType`, `durationHours` | 완료 |
| `PATCH` | `/api/admin/member-reports/{reportId}/reject` | 신고 반려 | Header: 관리자 인증 정보, Path: `reportId`, Body: `reviewComment`, `restrictionType`, `durationHours` | 완료 |

## 1. 회원 신고 생성
### `POST /api/member-reports`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |
| Body | `reportedMemberId` | UUID | Y |
| Body | `reason` | String | Y |
| Body | `reportType` | Enum | Y |

#### Request JSON
```json
{
  "reportedMemberId": "22222222-2222-2222-2222-222222222222",
  "reason": "욕설과 스팸 메시지를 반복했습니다.",
  "reportType": "ABUSE"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "reportId": "33333333-3333-3333-3333-333333333333",
    "reporterId": "11111111-1111-1111-1111-111111111111",
    "reportedMemberId": "22222222-2222-2222-2222-222222222222",
    "reason": "욕설과 스팸 메시지를 반복했습니다.",
    "reportType": "ABUSE",
    "status": "PENDING",
    "reviewComment": null,
    "reviewedBy": null,
    "reviewedAt": null,
    "createdAt": "2026-04-10T11:40:00",
    "updatedAt": "2026-04-10T11:40:00"
  },
  "error": null
}
```

## 2. 내 신고 목록 조회
### `GET /api/member-reports/me`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token | Y |

#### Response JSON
```json
{
  "success": true,
  "data": [
    {
      "reportId": "33333333-3333-3333-3333-333333333333",
      "reporterId": "11111111-1111-1111-1111-111111111111",
      "reportedMemberId": "22222222-2222-2222-2222-222222222222",
      "reason": "욕설과 스팸 메시지를 반복했습니다.",
      "reportType": "ABUSE",
      "status": "PENDING",
      "reviewComment": null,
      "reviewedBy": null,
      "reviewedAt": null,
      "createdAt": "2026-04-10T11:40:00",
      "updatedAt": "2026-04-10T11:40:00"
    }
  ],
  "error": null
}
```

## 3. 전체 신고 목록 조회
### `GET /api/admin/member-reports`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |

#### Response JSON
```json
{
  "success": true,
  "data": [
    {
      "reportId": "33333333-3333-3333-3333-333333333333",
      "reporterId": "11111111-1111-1111-1111-111111111111",
      "reportedMemberId": "22222222-2222-2222-2222-222222222222",
      "reason": "욕설과 스팸 메시지를 반복했습니다.",
      "reportType": "ABUSE",
      "status": "PENDING",
      "reviewComment": null,
      "reviewedBy": null,
      "reviewedAt": null,
      "createdAt": "2026-04-10T11:40:00",
      "updatedAt": "2026-04-10T11:40:00"
    }
  ],
  "error": null
}
```

## 4. 신고 상세 조회
### `GET /api/admin/member-reports/{reportId}`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Path | `reportId` | UUID | Y |

#### Response JSON
```json
{
  "success": true,
  "data": {
    "reportId": "33333333-3333-3333-3333-333333333333",
    "reporterId": "11111111-1111-1111-1111-111111111111",
    "reportedMemberId": "22222222-2222-2222-2222-222222222222",
    "reason": "욕설과 스팸 메시지를 반복했습니다.",
    "reportType": "ABUSE",
    "status": "PENDING",
    "reviewComment": null,
    "reviewedBy": null,
    "reviewedAt": null,
    "createdAt": "2026-04-10T11:40:00",
    "updatedAt": "2026-04-10T11:40:00"
  },
  "error": null
}
```

## 5. 특정 회원 대상 신고 목록 조회
### `GET /api/admin/member-reports/members/{memberId}`

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
      "reportId": "33333333-3333-3333-3333-333333333333",
      "reporterId": "11111111-1111-1111-1111-111111111111",
      "reportedMemberId": "22222222-2222-2222-2222-222222222222",
      "reason": "욕설과 스팸 메시지를 반복했습니다.",
      "reportType": "ABUSE",
      "status": "PENDING",
      "reviewComment": null,
      "reviewedBy": null,
      "reviewedAt": null,
      "createdAt": "2026-04-10T11:40:00",
      "updatedAt": "2026-04-10T11:40:00"
    }
  ],
  "error": null
}
```

## 6. 관리자 신고 승인
### `PATCH /api/admin/member-reports/{reportId}/approve`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Path | `reportId` | UUID | Y |
| Body | `reviewComment` | String | N |
| Body | `restrictionType` | Enum | N |
| Body | `durationHours` | Integer | N |

#### Request JSON
```json
{
  "reviewComment": "재현 가능하여 신고를 승인합니다.",
  "restrictionType": "LOGIN_BAN",
  "durationHours": 72
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "reportId": "33333333-3333-3333-3333-333333333333",
    "reporterId": "11111111-1111-1111-1111-111111111111",
    "reportedMemberId": "22222222-2222-2222-2222-222222222222",
    "reason": "욕설과 스팸 메시지를 반복했습니다.",
    "reportType": "ABUSE",
    "status": "APPROVED",
    "reviewComment": "재현 가능하여 신고를 승인합니다.",
    "reviewedBy": "99999999-9999-9999-9999-999999999999",
    "reviewedAt": "2026-04-10T11:50:00",
    "createdAt": "2026-04-10T11:40:00",
    "updatedAt": "2026-04-10T11:50:00"
  },
  "error": null
}
```

## 7. 관리자 신고 반려
### `PATCH /api/admin/member-reports/{reportId}/reject`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Header | `Authorization` | Bearer Token(Admin) | Y |
| Path | `reportId` | UUID | Y |
| Body | `reviewComment` | String | N |
| Body | `restrictionType` | Enum | N |
| Body | `durationHours` | Integer | N |

#### Request JSON
```json
{
  "reviewComment": "증빙이 부족하여 반려합니다.",
  "restrictionType": null,
  "durationHours": null
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "reportId": "33333333-3333-3333-3333-333333333333",
    "reporterId": "11111111-1111-1111-1111-111111111111",
    "reportedMemberId": "22222222-2222-2222-2222-222222222222",
    "reason": "욕설과 스팸 메시지를 반복했습니다.",
    "reportType": "ABUSE",
    "status": "REJECTED",
    "reviewComment": "증빙이 부족하여 반려합니다.",
    "reviewedBy": "99999999-9999-9999-9999-999999999999",
    "reviewedAt": "2026-04-10T11:50:00",
    "createdAt": "2026-04-10T11:40:00",
    "updatedAt": "2026-04-10T11:50:00"
  },
  "error": null
}
```
