# Gateway Service

## Table of Contents

- [1. 개요](#1-개요)
- [2. 소유 도메인 / 데이터](#2-소유-도메인--데이터)
- [3. 주요 유스케이스](#3-주요-유스케이스)
- [4. API 표면](#4-api-표면)
- [5. 서비스 내부 요청 흐름](#5-서비스-내부-요청-흐름)
- [6. 이벤트 연동](#6-이벤트-연동)
  - [6.1 발행 이벤트](#61-발행-이벤트)
  - [6.2 소비 이벤트](#62-소비-이벤트)
  - [6.3 실패 처리](#63-실패-처리)
- [7. 외부 의존성](#7-외부-의존성)
- [8. 보안 / 인가](#8-보안--인가)
- [9. 트랜잭션 / 일관성](#9-트랜잭션--일관성)
- [10. 운영 메모](#10-운영-메모)
- [11. 관련 파일](#11-관련-파일)
- [12. 관련 문서](#12-관련-문서)

---

## 1. 개요

Gateway Service는 클라이언트 요청의 단일 진입점이다. 이 서비스는 비즈니스 상태를 처리하지 않고, 요청을 각 도메인 서비스로 라우팅하기 전에 인증과 기본 접근 제어를 수행한다.

핵심 책임:

- 서비스별 경로 라우팅
- JWT access token 검증
- 공개 경로와 role rule 적용
- `X-Member-Id`, `X-Member-Role`, `X-Session-Id` 헤더 전달
- Redis 기반 access token / session blacklist 조회
- 전역 요청/응답 로깅
- Swagger 문서 집계와 프론트엔드용 CORS 정책 제공

Gateway는 인증 진입점이지만, 세션 생성이나 토큰 발급은 하지 않는다. 토큰 발급과 blacklist 기록은 Member Service가 담당하고, Gateway는 검증과 전달만 담당한다.

---

## 2. 소유 도메인 / 데이터

Gateway는 비즈니스 도메인 엔티티를 소유하지 않는다.

현재 runtime에서 참조하는 상태:

- `gateway.auth.public-paths`
- `gateway.auth.public-rules`
- `gateway.auth.role-rules`
- JWT 검증용 `jwt.secret`, `jwt.issuer`
- Redis의 access token blacklist 키
- Redis의 session blacklist 키

즉, Gateway는 영속 원본 데이터를 만드는 서비스가 아니라 요청 진입 규칙과 인증 상태를 해석하는 서비스다.

---

## 3. 주요 유스케이스

- 회원가입, 로그인, 상품 조회 같은 공개 API를 인증 없이 통과시킨다.
- 인증이 필요한 API에 대해 bearer token을 검증한다.
- 요청 경로와 HTTP method를 기준으로 role rule을 적용한다.
- 검증된 사용자 문맥을 downstream service가 읽을 수 있는 헤더로 변환한다.
- 서비스별 Swagger 문서를 `/swagger/**` 경로로 집계한다.

---

## 4. API 표면

Gateway는 자체 비즈니스 controller를 제공하지 않는다. 외부에 보이는 API 표면은 주로 라우팅 규칙과 Swagger 집계 경로다.

주요 라우팅 그룹:

| Path Group | Downstream |
|---|---|
| `/api/auth/**`, `/api/members/**`, `/api/sellers/**`, `/api/member-reports/**`, `/api/admin/member-reports/**`, `/api/admin/member-restrictions/**` | `member:8083` |
| `/api/products/**`, `/api/categories/**`, `/api/admin/products/**` | `product:8081` |
| `/api/carts/**`, `/api/wishes/**` | `cart:8086` |
| `/api/orders/**`, `/api/deliveries/**`, `/api/return-requests/**` | `order:8084` |
| `/api/payments/**` | `payment:8082` |
| `/api/settlements/**` | `settlement:8085` |
| `/api/auctions/**` | `auction:8090` |
| `/api/notifications/**` | `notification:8087` |
| `/api/ai/**` | `ai:8088` |

주요 Swagger 집계 경로:

| Path | Target |
|---|---|
| `/swagger/member/v3/api-docs` | member service OpenAPI |
| `/swagger/payment/v3/api-docs` | payment service OpenAPI |
| `/swagger/settlement/v3/api-docs` | settlement service OpenAPI |
| `/swagger/product/v3/api-docs` | product service OpenAPI |
| `/swagger/cart/v3/api-docs` | cart service OpenAPI |
| `/swagger/order/v3/api-docs` | order service OpenAPI |
| `/swagger/notification/v3/api-docs` | notification service OpenAPI |
| `/swagger/ai/v3/api-docs` | ai service OpenAPI |

특징:

- 인증/인가 규칙은 Java 코드보다 `application.yml`의 path rule 비중이 크다.
- `/api/`로 시작하지 않는 경로는 JWT 필터를 우회한다.
- `OPTIONS` preflight 요청은 그대로 통과한다.

---

## 5. 서비스 내부 요청 흐름

대표 흐름:

1. 공개 API 요청
   - `JwtAuthenticationFilter`가 `/api/**` 경로인지 먼저 확인한다.
   - `publicPaths` 또는 `publicRules`와 일치하면 토큰 없이 바로 downstream으로 전달한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Logging as RequestLoggingFilter
       participant Auth as JwtAuthenticationFilter
       participant Downstream

       Client->>Logging: public API request
       Logging->>Auth: filter(request)
       Auth->>Auth: /api 경로 여부 확인
       Auth->>Auth: public path / public rule 매칭
       Auth->>Downstream: forward without JWT validation
       Downstream-->>Client: response
   ```

2. 인증 필요 API 요청
   - `Authorization: Bearer <token>` 헤더 존재 여부를 검사한다.
   - `GatewayJwtValidator`가 issuer, signature, 만료, tokenType, blacklist를 검증한다.
   - `roleRules`로 현재 경로와 method에 허용된 역할인지 확인한다.
   - 통과 시 `X-Member-Id`, `X-Member-Role`, `X-Session-Id`를 추가해 downstream으로 전달한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Logging as RequestLoggingFilter
       participant Auth as JwtAuthenticationFilter
       participant Validator as GatewayJwtValidator
       participant Redis as TokenBlacklistStore
       participant Downstream

       Client->>Logging: authenticated API request
       Logging->>Auth: filter(request)
       Auth->>Auth: Authorization header 검사
       Auth->>Validator: validateAccessToken(token)
       Validator->>Validator: issuer / signature / expiry / tokenType 검증
       Validator->>Redis: blacklist check(accessTokenId, sessionId)
       Redis-->>Validator: blacklisted 여부
       Validator-->>Auth: AuthenticatedPrincipal
       Auth->>Auth: roleRules 매칭
       Auth->>Downstream: forward with X-Member-Id / X-Member-Role / X-Session-Id
       Downstream-->>Client: response
   ```

3. 오류 응답 반환
   - 토큰이 없거나 유효하지 않으면 `401 Unauthorized`를 JSON 형태로 반환한다.
   - role rule을 통과하지 못하면 `403 Forbidden`을 반환한다.
   - `RequestLoggingFilter`는 route ID와 status를 로그에 남긴다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Logging as RequestLoggingFilter
       participant Auth as JwtAuthenticationFilter
       participant Validator as GatewayJwtValidator

       Client->>Logging: protected API request
       Logging->>Auth: filter(request)
       alt Authorization header missing or invalid
           Auth-->>Client: 401 AuthErrorResponse
       else token validation failed
           Auth->>Validator: validateAccessToken(token)
           Validator-->>Auth: AuthException
           Auth-->>Client: 401 AuthErrorResponse
       else role rule denied
           Auth->>Validator: validateAccessToken(token)
           Validator-->>Auth: AuthenticatedPrincipal
           Auth-->>Client: 403 AuthErrorResponse
       end
   ```

---

## 6. 이벤트 연동

### 6.1 발행 이벤트

Gateway Service는 현재 도메인 이벤트를 발행하지 않는다.

---

### 6.2 소비 이벤트

Gateway Service는 현재 Kafka 등의 도메인 이벤트를 소비하지 않는다.

---

### 6.3 실패 처리

현재 실패 처리 전략:

- 인증 헤더 누락, 토큰 파싱 실패, 만료 토큰은 `AuthErrorResponse` 기반 `401`로 반환한다.
- role rule 불일치는 `403`으로 반환한다.
- Redis blacklist 조회 실패는 별도 우회 로직 없이 요청 실패로 이어질 수 있다.
- 라우팅 대상 서비스 장애는 Gateway 자체에서 복구하지 않고 downstream 오류로 전파된다.

---

## 7. 외부 의존성

- Redis: access token / session blacklist 조회
- Member Service와 공유하는 JWT issuer / secret 규약
- 각 downstream HTTP 서비스
- springdoc OpenAPI endpoint
- common-monitoring: Actuator / Prometheus 메트릭 노출

---

## 8. 보안 / 인가

- 공개 여부 판정은 `publicPaths`와 `publicRules`로 처리한다.
- 인증이 필요한 요청은 access token만 허용한다. refresh token은 통과하지 않는다.
- blacklist 검증은 access token ID와 session ID 두 축으로 수행한다.
- 세부 인가 규칙은 `roleRules`에 정의되고, Gateway는 path/method/role 수준의 1차 검사만 수행한다.
- 리소스 소유권 같은 도메인별 세부 인가는 downstream service가 담당한다.

---

## 9. 트랜잭션 / 일관성

- Gateway는 DB transaction을 사용하지 않는다.
- 요청 단위로 JWT 검증과 헤더 주입이 끝나는 stateless 처리에 가깝다.
- blacklist 반영은 Redis 상태에 의존하므로, Member Service가 blacklist를 기록한 직후 Gateway에서 바로 읽는 구조를 전제로 한다.
- 인증 설정은 `application.yml` 중심이라, rule 변경 시 코드보다 설정 배포 일관성이 중요하다.

---

## 10. 운영 메모

- 현재 route URI는 서비스 디스커버리 대신 `http://member:8083` 같은 고정 내부 주소를 사용한다.
- Swagger 집계 대상은 `member`, `payment`, `settlement`, `product`, `cart`, `order`, `notification`, `ai`까지만 포함되어 있다.
- `gateway.auth.role-rules` 누락 시 인증된 요청이어도 기본적으로 거부된다.
- `RequestLoggingFilter`는 route ID 기준으로 요청/응답 로그를 남기므로, 운영 시 경로 오배치 확인에 유용하다.
- `jwt.secret`가 비어 있거나 placeholder 상태면 `GatewayJwtValidator` 초기화에서 실패한다.

---

## 11. 관련 파일

- [application.yml](/C:/my_project/GoodsMall_BE/service/gateway/src/main/resources/application.yml)
- [JwtAuthenticationFilter.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/filter/JwtAuthenticationFilter.java)
- [RequestLoggingFilter.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/filter/RequestLoggingFilter.java)
- [GatewayJwtValidator.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/security/GatewayJwtValidator.java)
- [GatewayAuthProperties.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/security/GatewayAuthProperties.java)
- [JwtProperties.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/security/JwtProperties.java)
- [RedisTokenBlacklistStore.java](/C:/my_project/GoodsMall_BE/service/gateway/src/main/java/com/todaylunch/gateway/security/RedisTokenBlacklistStore.java)

---

## 12. 관련 문서

- [02-architecture.md](/C:/my_project/GoodsMall_BE/docs/02-architecture.md)
- [04-request-flow.md](/C:/my_project/GoodsMall_BE/docs/04-request-flow.md)
- [06-auth-flow.md](/C:/my_project/GoodsMall_BE/docs/06-auth-flow.md)
- [08-deployment.md](/C:/my_project/GoodsMall_BE/docs/08-deployment.md)
- [AuthDesign.md](/C:/my_project/GoodsMall_BE/service/gateway/docs/AuthDesign.md)
