# Project Auth Design

## 1. 개요

이 문서는 프로젝트 전반의 인증(Authentication) 및 인가(Authorization) 구조를 설명한다.
구현 중심 축은 `member-service`, `gateway-service`, `common-security` 이며, 실제 비즈니스 서비스는 게이트웨이가 전달한 인증 정보를 사용해 인가를 수행한다.

## 2. 인증/인가 책임 분리

| 영역              | 책임                                                   |
| ----------------- | ------------------------------------------------------ |
| `member-service`  | 로그인, 토큰 발급, 토큰 재발급, 로그아웃               |
| `gateway-service` | Access Token 검증, 공개 경로 예외 처리, 인증 헤더 주입 |
| `common-security` | 헤더 기반 현재 사용자 해석, 컨트롤러 주입              |
| 각 도메인 서비스  | 역할 기반 인가, 자원 소유자 검증, 도메인 정책 검증     |

## 3. 사용자 유형

| 사용자 유형 | 설명               | 주요 권한                               |
| ----------- | ------------------ | --------------------------------------- |
| `USER`      | 일반 사용자        | 내 정보 조회/수정, 신고 생성, 알림 조회 |
| `SELLER`    | 판매자 권한 사용자 | 판매자 정보 조회, 판매 도메인 기능 사용 |
| `ADMIN`     | 관리자             | 신고 검토, 회원 제재 생성/해제          |

## 4. 인증/인가 구성 요소

| 모듈              | 구성 요소                       | 역할                                                                |
| ----------------- | ------------------------------- | ------------------------------------------------------------------- |
| `member-service`  | `AuthController`, `AuthService` | 로그인, 토큰 재발급, 로그아웃 처리                                  |
| `member-service`  | `JwtTokenProvider`              | Access/Refresh Token 발급 및 Refresh Token 검증                     |
| `member-service`  | `RedisRefreshTokenStore`        | 회원별 Refresh Token 저장                                           |
| `member-service`  | `MemberRestrictionService`      | 로그인 금지 제재 확인                                               |
| `gateway-service` | `JwtAuthenticationFilter`       | 공용 인증 진입점, JWT 검증, 인증 헤더 주입                          |
| `gateway-service` | `GatewayJwtValidator`           | Access Token 서명, issuer, tokenType 검증                           |
| `gateway-service` | `GatewayAuthProperties`         | 공개 경로, JWT 검증 활성화 여부 관리                                |
| `common-security` | `CurrentMemberArgumentResolver` | `X-Member-Id`, `X-Member-Role` 헤더를 `AuthenticatedMember` 로 변환 |
| `common-security` | `@CurrentMember`                | 컨트롤러에서 현재 사용자 주입                                       |
| 도메인 서비스     | 서비스 계층 검증 로직           | 역할 기반 인가, 자원 소유자 검증                                    |

## 5. 토큰 구조

| 항목              | Access Token     | Refresh Token      |
| ----------------- | ---------------- | ------------------ |
| issuer            | `member-service` | `member-service`   |
| subject           | `memberId`       | `memberId`         |
| `memberId` claim  | 포함             | 포함               |
| `email` claim     | 포함             | 미포함             |
| `role` claim      | 포함             | 미포함             |
| `tokenType` claim | `ACCESS`         | `REFRESH`          |
| 저장 위치         | 클라이언트       | 클라이언트 + Redis |

### 5.1. JWT claim 예시

```json
{
  "sub": "11111111-1111-1111-1111-111111111111",
  "memberId": "11111111-1111-1111-1111-111111111111",
  "email": "user@test.local",
  "role": "USER",
  "tokenType": "ACCESS",
  "iss": "member-service"
}
```

## 6. 서비스 내부 인증 컨텍스트 생성

### 6.1. 처리 주체

- `common-security`
- `CurrentMemberWebConfig`
- `CurrentMemberArgumentResolver`
- `@CurrentMember`

### 6.2. 동작 방식

1. 게이트웨이가 `X-Member-Id`, `X-Member-Role` 헤더를 전달
2. 각 MVC 서비스가 `CurrentMemberWebConfig` 를 import
3. `CurrentMemberArgumentResolver` 가 요청 헤더를 읽음
4. `AuthenticatedMember(memberId, role)` 객체 생성
5. 컨트롤러 메서드 파라미터에 주입

### 6.3. 사용 예시

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
    @CurrentMember AuthenticatedMember authenticatedMember
) {
    return ResponseEntity.ok(ApiResponse.success(
        memberUsecase.getCurrentMember(authenticatedMember.memberId())
    ));
}
```

### 6.4. 헤더 검증 실패 시

- `X-Member-Id`, `X-Member-Role` 누락
- UUID 파싱 실패
- `MemberRole` enum 변환 실패
- 위 경우 모두 `InvalidTokenException`

## 7. 인가 설계

### 7.1. 인가 기준

| 기준                  | 예시                                         |
| --------------------- | -------------------------------------------- |
| 역할 기반 인가        | 관리자만 신고 검토, 제재 생성 가능           |
| 소유자 기반 인가      | 알림은 본인 것만 읽음 처리 가능              |
| 도메인 정책 기반 인가 | 로그인 금지 제재가 있으면 로그인/재발급 차단 |

### 7.2. 현재 구현 예시

- `MemberRestrictionService.validateAdmin()` 에서 `ADMIN` 여부 검증
- `MemberReportService.validateAdmin()` 에서 관리자 API 접근 제어
- `NotificationService.markAsRead()` 에서 본인 소유 알림인지 검증
- `SellerService.registerSeller()` 후 `MemberRole.SELLER` 로 역할 전환

## 8. 인증/인가 예외 처리

### 8.1. 주요 예외

| 예외                            | 발생 상황                              |
| ------------------------------- | -------------------------------------- |
| `InvalidLoginException`         | 이메일/비밀번호 불일치                 |
| `InvalidTokenException`         | 토큰 파싱 실패, 타입 오류, 헤더 불일치 |
| `RefreshTokenNotFoundException` | Redis 에 Refresh Token 없음            |
| `MemberRestrictedException`     | 로그인 금지 제재 활성                  |
| `AdminAccessDeniedException`    | 관리자 권한 없음                       |

### 8.2. 응답 전략

- 게이트웨이에서 인증 실패 시 즉시 `401 Unauthorized`
- 서비스 내부에서 헤더 기반 사용자 해석 실패 시 `InvalidTokenException`
- 역할 부족, 리소스 접근 불가 등은 도메인 예외로 처리

## 9. 프로젝트 전반 관점의 장점

- 인증 검증을 게이트웨이에 집중시켜 서비스별 중복 구현을 줄인다.
- 서비스는 JWT 파싱보다 비즈니스 인가에 집중할 수 있다.
- 공통 보안 모듈을 통해 컨트롤러 사용성이 단순해진다.
- Redis 기반 Refresh Token 관리로 토큰 재발급 흐름을 제어할 수 있다.

## 10. 현재 한계와 개선 포인트

- Access Token 블랙리스트 미구현. 🟡
- 로그아웃이 Access Token 즉시 무효화를 보장하지 않음. 🟡
- 게이트웨이 공개 경로 정책과 각 서비스 API 정책 간 정합성 점검 필요. 🟡
- 서비스 간 내부 호출에 대한 별도 서비스 인증 체계는 아직 없음. 🟡
- 세밀한 정책 기반 인가(RBAC/ABAC) 보다는 서비스 코드 내 조건 검증 중심 구조다.

## 11. 설계 요약

- 로그인과 토큰 발급은 `member-service` 가 담당한다.
- 요청 인증은 `gateway-service` 가 담당한다.
- 인증 결과를 각 서비스는 `common-security` 로 해석한다.
- 실제 권한 판단은 각 도메인 서비스가 수행한다.
- 즉, 이 프로젝트의 인증/인가 구조는 `토큰 발급`, `게이트웨이 검증`, `헤더 기반 사용자 주입`, `도메인 인가` 의 4단계로 구성된다.

## 12. 인증 흐름

### [1] 로그인

```text
Client
  -> POST /api/auth/login
member-service(AuthService)
  -> 이메일/비밀번호 검증
  -> LOGIN_BAN 제재 확인
  -> Access Token 발급
  -> Refresh Token 발급
  -> Redis 에 Refresh Token 저장
  -> Client 에 토큰 응답
```

### [2] 토큰 재발급

```text
Client
  -> POST /api/auth/refresh
member-service(AuthService)
  -> Refresh Token 서명 검증
  -> tokenType=REFRESH 검증
  -> memberId 추출
  -> LOGIN_BAN 제재 확인
  -> Redis 저장 토큰과 비교
  -> 새 Access Token 발급
  -> Client 에 재발급 응답
```

### [3] 게이트웨이 인증

```
Client
  -> Authorization: Bearer <access-token>
  -> gateway-service
gateway-service(JwtAuthenticationFilter)
  -> 공개 경로 여부 확인
  -> Access Token issuer/signature/expiration/tokenType 검증
  -> X-Member-Id, X-Member-Role 헤더 추가
  -> downstream service 전달
common-security(CurrentMemberArgumentResolver)
  -> AuthenticatedMember 생성
Domain Service
  -> 역할 기반 인가
  -> 자원 소유자 검증
```

### [4] 미구현/결정 필요

```
Access Token 블랙리스트와 즉시 무효화는 아직 구현되어 있지 않다. 🟡
```

## 13. 관련 문서

- 게이트웨이 전용 인증 설계: [AuthDesign.md](/c:/my_project/beadv5_2_TodayLunchMenu_BE/gateway/docs/AuthDesign.md)
