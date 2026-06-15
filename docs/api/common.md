# API Common Rules

## 목차

- [1. 목적](#1-목적)
- [2. 현재 적용 상태 요약](#2-현재-적용-상태-요약)
- [3. 인증](#3-인증)
- [4. 공통 Header](#4-공통-header)
- [5. 성공 응답](#5-성공-응답)
- [6. 에러 응답](#6-에러-응답)
- [7. Validation Error](#7-validation-error)
- [8. Pagination](#8-pagination)
- [9. Sorting](#9-sorting)
- [10. Date and Time](#10-date-and-time)
- [11. ID Format](#11-id-format)
- [12. Breaking Change](#12-breaking-change)
- [13. OpenAPI 작성 기준](#13-openapi-작성-기준)

<br>

---

## 1. 목적

이 문서는 GoodsMall API spec에서 공통으로 다룰 규칙을 정리한다.

현재 프로젝트는 서비스별 응답 래퍼, 에러 응답, 페이징 방식이 완전히 통일되어 있지 않다. 따라서 이 문서는 현재 구현 상태를 기록하면서, 앞으로 OpenAPI 정리와 API 표준화 과정에서 맞춰갈 권장 표준을 함께 정의한다.

<br>

---

## 2. 현재 적용 상태 요약

현재 적용 상태:

- `member`, `payment`, `order`, `ai`, `settlement`, `notification` 서비스는 대체로 `success`, `data`, `error` 형태의 응답 래퍼를 사용한다.
- `product`, `cart` 서비스는 공통 응답 래퍼보다 별도 `ErrorResponse`를 중심으로 에러를 반환한다.
- 대부분의 서비스 에러 응답에는 `traceId`, `details`가 공통 필드로 들어가 있지 않다.
- validation 에러 코드는 서비스별로 다르다. 예를 들어 `member`는 `VALIDATION_ERROR`, 일부 서비스는 `INVALID_INPUT_VALUE` 계열을 사용한다.
- 목록 API는 Spring `Pageable`, `Page<T>`, 커스텀 `PagedResponse<T>`가 혼재되어 있다.
- OpenAPI 어노테이션 정리는 현재 일부 API부터 진행 중이며, 모든 서비스에 동일하게 적용된 상태는 아니다.

권장 표준 / 정리 예정:

- 신규 또는 정리 대상 API부터 응답, 에러, 페이징, 정렬 규칙을 점진적으로 통일한다.
- 문서에는 현재 구현과 목표 표준이 섞이지 않도록 `현재 적용 상태`와 `권장 표준 / 정리 예정`을 구분해서 작성한다.
- 공통 응답 형식을 변경할 때는 기존 클라이언트 영향도를 확인하고 별도 마이그레이션 계획을 둔다.

<br>

---

## 3. 인증

현재 적용 상태:

- 인증이 필요한 API는 Bearer access token을 사용한다.

```http
Authorization: Bearer <access-token>
```

- Gateway를 통해 접근하는 API는 Gateway에서 JWT를 검증하고 서비스별 요청으로 전달하는 구조를 사용한다.
- 일부 OpenAPI 문서에는 인증 사용자 파라미터가 아직 내부 구현 형태로 노출될 수 있다.

권장 표준 / 정리 예정:

- 인증 필요 API는 OpenAPI security scheme을 명시한다.
- 공개 API와 인증 필요 API는 tag 또는 operation 설명에서 구분한다.
- 관리자 권한이 필요한 API는 필요한 role을 짧고 명확하게 표시한다.
- 컨트롤러 내부에서 주입되는 인증 사용자 객체는 API request parameter로 노출하지 않는다.

<br>

---

## 4. 공통 Header

현재 적용 상태:

- JSON API는 대체로 `Content-Type: application/json`과 `Accept: application/json`을 사용한다.
- 인증 API는 `Authorization` header를 사용한다.
- request id 또는 trace id header는 아직 모든 서비스에 공통 규칙으로 적용되어 있지 않다.

권장 표준 / 정리 예정:

| Header | Required | Description |
|---|---:|---|
| `Authorization` | 조건부 | Bearer access token |
| `Content-Type` | 조건부 | JSON request body가 있는 경우 `application/json` |
| `Accept` | 권장 | `application/json` |

추적용 request id 또는 trace id를 도입할 경우 모든 서비스에서 동일한 header 이름을 사용한다.

<br>

---

## 5. 성공 응답

현재 적용 상태:

- 여러 서비스가 `success`, `data`, `error` 형태의 래퍼를 사용한다.
- 일부 서비스는 resource object 또는 Spring `Page<T>`를 직접 반환한다.
- 서비스별로 생성, 수정, 삭제 API의 응답 형식이 완전히 통일되어 있지는 않다.

권장 표준 / 정리 예정:

- 단건 조회는 resource object를 반환한다.
- 목록 조회는 pagination metadata 포함 여부를 명확히 한다.
- 생성 API는 생성된 resource 또는 생성 식별자를 반환한다.
- 상태 변경 API는 변경 결과를 확인할 수 있는 응답을 반환한다.
- 단순 삭제 API는 `204 No Content` 또는 명시적 결과 응답 중 하나로 통일한다.

<br>

---

## 6. 에러 응답

현재 적용 상태:

- `member`, `payment`, `order`, `ai`, `settlement`, `notification` 계열은 대체로 `error.code`, `error.message` 구조를 사용한다.
- `product`, `cart` 계열은 `status`, `error`, `message`, `errorCode`, `path`, `timestamp`, `errors` 구조를 사용한다.
- `traceId`는 대부분의 서비스에서 공통 응답 필드로 제공되지 않는다.

권장 표준 / 정리 예정:

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
| `code` | 필수 | 서비스 또는 도메인 기준 에러 코드 |
| `message` | 필수 | 사용자 또는 클라이언트가 이해할 수 있는 에러 메시지 |
| `traceId` | 권장 | 로그 추적을 위한 식별자 |
| `details` | 조건부 | validation field error 등 추가 정보 |

에러 코드 원칙:

- 대문자 snake case를 사용한다.
- 도메인 또는 리소스 이름을 포함한다.
- 같은 상황에는 서비스별로 다른 코드를 만들지 않는다.

예시:

```text
MEMBER_NOT_FOUND
PRODUCT_NOT_FOUND
ORDER_ALREADY_CANCELED
PAYMENT_AMOUNT_MISMATCH
UNAUTHORIZED
FORBIDDEN
VALIDATION_ERROR
```

<br>

---

## 7. Validation Error

현재 적용 상태:

- validation 에러 응답 형식과 에러 코드가 서비스별로 다르다.
- `member`는 `VALIDATION_ERROR`를 사용한다.
- `product`, `cart` 계열은 `errors` 배열에 field 단위 오류를 포함하는 구조를 사용한다.

권장 표준 / 정리 예정:

validation error는 field 단위 오류를 표현할 수 있어야 한다.

```json
{
  "code": "VALIDATION_ERROR",
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

<br>

---

## 8. Pagination

현재 적용 상태:

- 서비스별로 Spring `Pageable`, Spring `Page<T>`, 커스텀 `PagedResponse<T>`가 혼재되어 있다.
- `page`, `size` query parameter는 여러 서비스에서 사용하지만, 최대 `size`와 기본 정렬 기준은 서비스별로 다르다.

권장 표준 / 정리 예정:

목록 API는 pagination 규칙을 명확히 한다.

| Parameter | Description |
|---|---|
| `page` | 0-based 또는 1-based 중 하나로 통일 |
| `size` | 페이지 크기 |
| `sort` | 정렬 기준 |

정리 필요 항목:

- `page`가 0-based인지 1-based인지 통일한다.
- 최대 `size` 제한을 명시한다.
- 기본 정렬 기준을 명시한다.
- 응답 metadata 형식을 통일한다.

<br>

---

## 9. Sorting

현재 적용 상태:

- 정렬 방식은 서비스별로 다르다.
- 일부 API는 Spring `Sort` 형식을 사용하고, 일부 API는 서비스 내부 고정 정렬을 사용한다.

권장 표준 / 정리 예정:

정렬 query는 다음 중 하나로 통일한다.

```text
sort=createdAt,desc
```

또는

```text
sortBy=createdAt&direction=desc
```

서비스별로 서로 다른 방식을 계속 늘리지 않도록 OpenAPI 정리 단계에서 맞춘다.

<br>

---

## 10. Date and Time

현재 적용 상태:

- Java `LocalDateTime`, `Instant`, 문자열 날짜가 서비스별 DTO에서 혼재될 수 있다.
- OpenAPI description에서 timezone 기준이 항상 명시되어 있지는 않다.

권장 표준 / 정리 예정:

날짜와 시간은 ISO-8601 형식을 기준으로 쓴다.

```text
2026-05-31T23:00:25+09:00
2026-05-31T14:00:25Z
```

원칙:

- 저장과 서비스 간 통신은 UTC 기준을 우선한다.
- 사용자 표시 시간은 클라이언트 또는 표시 계층에서 변환한다.
- API field description에 timezone 기준을 명시한다.

<br>

---

## 11. ID Format

현재 적용 상태:

- 리소스 식별자는 서비스별 저장 타입을 따른다.
- UUID와 숫자 ID가 함께 사용될 수 있다.

권장 표준 / 정리 예정:

- UUID라면 `format: uuid`를 명시한다.
- 숫자 ID라면 범위와 의미를 명시한다.
- 내부 PG, 배송사, OAuth provider ID 같은 외부 ID와 내부 리소스 ID를 구분한다.

<br>

---

## 12. Breaking Change

현재 적용 상태:

- API 변경 시 breaking change 여부를 문서에서 일관되게 표시하는 규칙은 아직 강제되어 있지 않다.

권장 표준 / 정리 예정:

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

<br>

---

## 13. OpenAPI 작성 기준

현재 적용 상태:

- member 주요 API부터 OpenAPI 어노테이션 정리를 진행 중이다.
- 모든 서비스에 동일한 annotation 스타일이 적용된 상태는 아니다.
- auction 등 일부 서비스는 추후 OpenAPI 적용 TODO가 남아 있다.

권장 표준 / 정리 예정:

외부 또는 프론트엔드가 사용하는 API부터 다음 항목을 정리한다.

- `@Tag`
- `@Operation`
- `@ApiResponses`
- request DTO `@Schema`
- response DTO `@Schema`
- 인증 필요 API의 security 설정

내부 batch, actuator, debug endpoint는 공개 API 문서와 분리한다.
