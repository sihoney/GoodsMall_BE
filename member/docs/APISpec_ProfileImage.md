# ProfileImage API Spec

## 엔드포인트
| Method | Endpoint | 설명 | 요청 시 필요한 데이터 | 구현 여부 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/profile-images/presign` | 프로필 이미지 업로드용 presigned URL 발급 | Body: `fileName`, `contentType` | 완료 |

## 1. 프로필 이미지 Presign URL 발급
### `POST /api/auth/profile-images/presign`

#### 요청 시 필요한 데이터
| 위치 | 필드 | 타입 | 필수 |
| --- | --- | --- | --- |
| Body | `fileName` | String | Y |
| Body | `contentType` | String | Y |

#### Request JSON
```json
{
  "fileName": "profile.png",
  "contentType": "image/png"
}
```

#### Response JSON
```json
{
  "success": true,
  "data": {
    "uploadUrl": "https://s3.amazonaws.com/bucket/presigned-url",
    "fileKey": "profiles/2026/04/uuid-profile.png",
    "expiresAt": "2026-04-10T11:30:00"
  },
  "error": null
}
```
