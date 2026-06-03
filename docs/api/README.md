# API Spec

## 목차

- [1. 목적](#1-목적)
- [2. 접근 방식](#2-접근-방식)
- [3. Gateway 통합 접근](#3-gateway-통합-접근)
- [4. 서비스별 직접 접근](#4-서비스별-직접-접근)
- [5. 문서 관리 원칙](#5-문서-관리-원칙)
- [6. OpenAPI 산출물](#6-openapi-산출물)
- [7. 관련 문서](#7-관련-문서)

<br>

---

## 1. 목적

이 문서는 GoodsMall 서비스의 API spec 확인 위치와 운영 원칙을 정리한다.

OpenAPI는 단순 Swagger UI 화면이 아니라 서비스별 API 계약 기준으로 사용한다. 상세 endpoint, request, response schema는 OpenAPI 문서를 기준으로 확인하고, Markdown 문서는 접근 경로와 공통 정책을 설명한다.

<br>

---

## 2. 접근 방식

로컬 개발과 운영/배포 환경의 OpenAPI 접근 방식은 다르게 본다.

| 환경 | 접근 방식 | 용도 |
|---|---|---|
| 로컬 개발 | 서비스 port 직접 접근 가능 | 개별 서비스 API spec 빠른 확인 |
| 로컬 개발 | Gateway 통합 접근 가능 | 여러 서비스 API spec을 하나의 Swagger UI에서 확인 |
| 운영/배포 | Gateway 통합 접근만 허용하는 것을 권장 | 서비스 port 직접 외부 노출 방지 |

`Gateway 통합 접근`은 Gateway가 서비스별 OpenAPI 문서를 프록시하는 문서 경로만 여는 방식을 말한다.

서비스 내부의 `/v3/api-docs`는 Gateway가 프록시할 때 사용하는 내부 경로로 보고, 운영/배포 환경에서는 각 서비스 port를 외부에 직접 노출하지 않는다.

<br>

---

## 3. Gateway 통합 접근

Gateway가 실행 중이면 다음 주소에서 서비스별 OpenAPI 문서를 선택해서 볼 수 있다.

```text
http://localhost:8080/swagger-ui.html
```

서비스별 OpenAPI JSON은 Gateway를 통해 다음 경로로 프록시된다.

| Service | Gateway OpenAPI URL |
|---|---|
| member | `http://localhost:8080/swagger/member/v3/api-docs` |
| product | `http://localhost:8080/swagger/product/v3/api-docs` |
| cart | `http://localhost:8080/swagger/cart/v3/api-docs` |
| order | `http://localhost:8080/swagger/order/v3/api-docs` |
| payment | `http://localhost:8080/swagger/payment/v3/api-docs` |
| settlement | `http://localhost:8080/swagger/settlement/v3/api-docs` |
| notification | `http://localhost:8080/swagger/notification/v3/api-docs` |
| ai | `http://localhost:8080/swagger/ai/v3/api-docs` |

Gateway 통합 접근을 사용하려면 Gateway와 대상 서비스가 함께 실행 중이어야 한다.

운영/배포 환경에서는 이 방식만 외부에 공개하고, 개별 서비스의 Swagger UI 또는 `/v3/api-docs`는 외부에서 직접 접근하지 못하도록 제한한다.

<br>

---

## 4. 서비스별 직접 접근

로컬 개발 중에는 개별 서비스 port로 OpenAPI 문서에 직접 접근할 수 있다.

이 방식은 개별 서비스만 빠르게 확인할 때 사용한다. 운영/배포 환경의 외부 공개 방식으로 사용하지 않는다.

| Service | Swagger UI | OpenAPI JSON |
|---|---|---|
| gateway | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |
| product | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| payment | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| member | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| order | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| settlement | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |
| cart | `http://localhost:8086/swagger-ui.html` | `http://localhost:8086/v3/api-docs` |
| notification | `http://localhost:8087/swagger-ui.html` | `http://localhost:8087/v3/api-docs` |
| ai | `http://localhost:8088/swagger-ui.html` | `http://localhost:8088/v3/api-docs` |
| auction | `http://localhost:8090/swagger-ui.html` | `http://localhost:8090/v3/api-docs` |

<br>

---

## 5. 문서 관리 원칙

- 상세 endpoint 목록은 수동으로 Markdown에 복사하지 않는다.
- request/response field는 OpenAPI schema를 기준으로 확인한다.
- Markdown 문서는 인증, 공통 응답, 에러, pagination, 변경 정책을 설명한다.
- API 변경 PR에서는 OpenAPI 변경 여부와 breaking change 여부를 확인한다.
- 운영 환경에서는 Swagger UI와 `/v3/api-docs` 노출 범위를 Gateway 기준으로 제한한다.

<br>

---

## 6. OpenAPI 산출물

<!-- TODO: 추후 OpenAPI JSON을 기반으로 TypeScript client generation 스크립트를 추가한다. 생성 결과는 backend repo에서는 검증용으로만 사용하고 gitignore 처리한다. 장기적으로 frontend repo로 이전한다. -->

OpenAPI JSON export 결과는 `docs/api/openapi/` 아래에 저장한다.

기본 export 명령:

```powershell
./scripts/export-openapi.ps1 member
```

기본값:

| 항목 | 값 |
|---|---|
| Service | `member` |
| URL | `http://localhost:8083/v3/api-docs` |
| Output | `docs/api/openapi/member.json` |

서비스 port를 직접 지정해야 하는 경우:

```powershell
./scripts/export-openapi.ps1 member -BaseUrl http://localhost:8083
```

예상 구조:

```text
docs/api/openapi/
  member.json
  product.json
  order.json
  payment.json
```

<br>

---

## 7. 관련 문서

- [common.md](common.md)
- [../03-service-responsibilities.md](../03-service-responsibilities.md)
- [../04-request-flow.md](../04-request-flow.md)
- [../06-auth-flow.md](../06-auth-flow.md)
- [../10-troubleshooting.md](../10-troubleshooting.md)
