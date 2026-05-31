# API Common Rules

## 목차

- [목적](#목적)
- [인증](#인증)
- [공통 Header](#공통-header)
- [성공 응답](#성공-응답)
- [에러 응답](#에러-응답)
- [Validation Error](#validation-error)
- [Pagination](#pagination)
- [Sorting](#sorting)
- [Date and Time](#date-and-time)
- [ID Format](#id-format)
- [Breaking Change](#breaking-change)
- [OpenAPI 작성 기준](#openapi-작성-기준)

## 목적

이 문서는 GoodsMall API spec에서 공통으로 맞춰야 할 규칙을 정리한다.

아직 모든 서비스가 동일한 응답 형식과 에러 형식을 완전히 공유하는 단계는 아니므로, 현재 문서는 API spec 정리를 위한 기준 문서로 사용한다. 실제 코드와 다른 항목은 이후 서비스별 OpenAPI 정리 단계에서 맞춘다.

## 인증

인증이 필요한 API는 Bearer access token을 사용한다.

```http
Authorization: Bearer <access-token>
```

Gateway를 통해 접근하는 API는 Gateway에서 JWT를 검증하고, 서비스별 요청으로 전달한다.

문서화 기준:

- 인증 필요 API는 OpenAPI security scheme을 명시한다.
- 공개 API와 인증 필요 API를 tag 또는 description에서 구분한다.
- 관리자 권한이 필요한 API는 role 요구사항을 description에 명시한다.

## 공통 Header

권장 header:

| Header | Required | Description |
|---|---:|---|
| `Authorization` | 조건부 | Bearer access token |
| `Content-Type` | 조건부 | JSON request body가 있는 경우 `application/json` |
| `Accept` | 권장 | `application/json` |

추적용 request id 또는 trace id를 도입하는 경우 모든 서비스에서 동일한 header 이름을 사용한다.

## 성공 응답

서비스별 응답 형식은 API 정리 단계에서 확인한다.

권장 원칙:

- 단건 조회는 resource object를 반환한다.
- 목록 조회는 pagination metadata 포함 여부를 명확히 한다.
- 생성 API는 생성된 resource 또는 식별자를 반환한다.
- 상태 변경 API는 변경 결과를 확인할 수 있는 응답을 반환한다.
- 단순 삭제 API는 `204 No Content` 또는 명시적 결과 응답 중 하나로 통일한다.

## 에러 응답

공통 에러 응답은 다음 형태를 기준으로 정리한다.

```json
{
  "code": "MEMBER_NOT_FOUND",
  "message": "회원을 찾을 수 없습니다.",
  "traceId": "01J..."
}
```

권장 필드:

| Field | Required | Description |
|---|---:|---|
| `code` | 예 | 서비스 또는 도메인 기준 에러 코드 |
| `message` | 예 | 사용자 또는 클라이언트가 이해할 수 있는 에러 메시지 |
| `traceId` | 권장 | 로그 추적을 위한 식별자 |
| `details` | 조건부 | validation field error 등 추가 정보 |

에러 코드 원칙:

- 대문자 snake case를 사용한다.
- 도메인 또는 리소스 이름을 포함한다.
- 동일한 상황에 대해 서비스별로 다른 코드를 만들지 않는다.

예시:

```text
MEMBER_NOT_FOUND
PRODUCT_NOT_FOUND
ORDER_ALREADY_CANCELED
PAYMENT_AMOUNT_MISMATCH
UNAUTHORIZED
FORBIDDEN
VALIDATION_FAILED
```

## Validation Error

validation error는 field 단위 오류를 표현할 수 있어야 한다.

권장 예시:

```json
{
  "code": "VALIDATION_FAILED",
  "message": "요청 값이 올바르지 않습니다.",
  "traceId": "01J...",
  "details": [
    {
      "field": "email",
      "reason": "이메일 형식이 올바르지 않습니다."
    }
  ]
}
```

OpenAPI에는 validation 실패 응답 예시를 명시한다.

## Pagination

목록 API는 pagination 규칙을 명확히 한다.

권장 query parameter:

| Parameter | Description |
|---|---|
| `page` | 0 또는 1 기준 중 하나로 통일 |
| `size` | 페이지 크기 |
| `sort` | 정렬 기준 |

정리 필요 항목:

- `page`가 0-based인지 1-based인지 통일한다.
- 최대 `size` 제한을 명시한다.
- 기본 정렬 기준을 명시한다.

## Sorting

정렬 query는 다음 중 하나로 통일한다.

```text
sort=createdAt,desc
```

또는

```text
sortBy=createdAt&direction=desc
```

서비스별로 서로 다른 방식을 쓰지 않도록 OpenAPI 정리 단계에서 맞춘다.

## Date and Time

날짜와 시간은 ISO-8601 형식을 기준으로 한다.

예시:

```text
2026-05-31T23:00:25+09:00
2026-05-31T14:00:25Z
```

권장 원칙:

- 저장과 서비스 간 통신은 UTC 기준을 우선한다.
- 사용자 표시 시간은 클라이언트 또는 표시 계층에서 변환한다.
- API field description에 timezone 기준을 명시한다.

## ID Format

리소스 식별자는 서비스별 실제 타입을 따른다.

문서화 기준:

- UUID이면 `format: uuid`를 명시한다.
- 숫자 ID이면 범위와 의미를 명시한다.
- 외부 PG, 배송사, OAuth provider ID는 내부 ID와 구분한다.

## Breaking Change

다음 변경은 breaking change로 본다.

- endpoint 삭제
- HTTP method 변경
- request 필수 필드 추가
- request field type 변경
- response field 삭제
- response field type 변경
- error code 변경
- 인증 필요 여부 변경
- pagination 또는 sorting 규칙 변경

API 변경 PR에서는 breaking change 여부를 명시한다.

## OpenAPI 작성 기준

외부 또는 프론트엔드가 사용하는 API부터 다음 항목을 정리한다.

- `@Tag`
- `@Operation`
- `@ApiResponses`
- request DTO `@Schema`
- response DTO `@Schema`
- 인증 필요 API의 security 설정

내부 batch, actuator, debug endpoint는 공개 API 문서와 분리한다.
