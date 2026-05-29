# Member Service

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

Member Service는 회원 계정과 인증 보조 상태를 소유하는 서비스다.

핵심 책임:

- 회원 가입, 프로필 조회/수정, 비밀번호 변경, 탈퇴
- 로그인, 토큰 재발급, 세션 조회/로그아웃
- 이메일 인증, 비밀번호 재설정, 계좌 인증
- 판매자 등록과 판매자 전환
- 카카오 OAuth 로그인/연동/연동 해제
- 회원 신고와 관리자 제재 관리

다른 도메인 서비스는 회원의 신원과 역할을 참조할 수 있지만, 회원 원본 상태 자체는 소유하지 않는다. 인증 토큰 발급과 세션/blacklist 관리는 Member Service가 생성하고 Gateway가 검증한다.

---

## 2. 소유 도메인 / 데이터

주요 영속 데이터:

- `Member`
- `Seller`
- `EmailVerification`
- `MemberOauthAccount`
- `MemberReport`
- `MemberRestriction`

주요 Redis 상태:

- 로그인 세션
- refresh token 식별자
- access token / session blacklist
- 카카오 OAuth authorize state / pending link
- 비밀번호 재설정 토큰
- 계좌 인증 세션
- 이메일 인증 auto-login 토큰
- 판매자 등록 draft

핵심 상태:

- 회원 상태: 활성, 정지, 탈퇴
- 판매자 등록/전환 상태
- 이메일 인증 완료 여부
- 계좌 인증 진행 상태
- 관리자 제재 활성 여부

---

## 3. 주요 유스케이스

- 일반 회원 가입 후 이메일 인증 완료
- 이메일/비밀번호 로그인과 refresh token rotation
- 현재 로그인 세션 종료 또는 전체 세션 종료
- 카카오 OAuth 로그인 또는 기존 계정 연동
- 판매자 등록 요청 후 계좌 인증 완료를 통한 판매자 승격
- 회원 탈퇴 전 주문/결제/상품/경매/정산 잔여 상태 검증
- 특정 회원 신고 접수와 관리자 제재 처리

---

## 4. API 표면

주요 외부 API:

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/members` | `POST` | 회원 가입 | public |
| `/api/members/me` | `GET` | 현재 회원 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/me` | `PATCH` | 현재 회원 수정 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/me/password` | `PATCH` | 현재 회원 비밀번호 변경 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/me` | `DELETE` | 현재 회원 탈퇴 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/me/oauth-accounts` | `GET` | 연동 OAuth 계정 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/me/oauth-accounts/{provider}` | `DELETE` | OAuth 계정 연동 해제 | `USER`, `SELLER`, `ADMIN` |
| `/api/members/{memberId}` | `GET` | 특정 회원 조회 | public |
| `/api/members/{memberId}` | `PATCH` | 특정 회원 수정 | gateway role rule 확인 필요 |
| `/api/auth/login` | `POST` | 로그인 | public |
| `/api/auth/refresh` | `POST` | 토큰 재발급 | public |
| `/api/auth/sessions` | `GET` | 현재 회원 세션 목록 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/auth/sessions/{sessionId}` | `DELETE` | 특정 세션 종료 | `USER`, `SELLER`, `ADMIN` |
| `/api/auth/logout/current` | `POST` | 현재 세션 로그아웃 | `USER`, `SELLER`, `ADMIN` |
| `/api/auth/logout/all` | `POST` | 전체 세션 로그아웃 | `USER`, `SELLER`, `ADMIN` |
| `/api/auth/password-resets` | `POST` | 비밀번호 재설정 메일 발송 | public |
| `/api/auth/password-resets/confirm` | `POST` | 비밀번호 재설정 확정 | public |
| `/api/auth/email-verifications` | `POST` | 이메일 인증 메일 발송 | public |
| `/api/auth/email-verifications/confirm` | `POST` | 이메일 인증 확정 | public |
| `/api/auth/email-verifications/auto-login` | `POST` | 이메일 인증 후 자동 로그인 | public |
| `/api/auth/oauth/kakao/**` | `GET`, `POST` | 카카오 OAuth 로그인/연동 | 일부 public, 일부 authenticated |
| `/api/auth/profile-images/presign` | `POST` | 프로필 이미지 업로드 presigned URL 발급 | public |
| `/api/sellers/register` | `POST` | 판매자 등록 요청 생성 | `USER`, `SELLER`, `ADMIN` |
| `/api/sellers/me` | `GET` | 현재 판매자 조회 | `SELLER`, `ADMIN` |
| `/api/members/me/account-verifications/**` | `GET`, `POST` | 계좌 인증 생성/조회/확인/재전송/취소 | `USER`, `SELLER`, `ADMIN` |
| `/api/member-reports` | `POST`, `GET` | 회원 신고 생성, 내 신고 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/admin/member-reports/**` | `GET`, `PATCH` | 신고 검토/승인/반려 | `ADMIN` |
| `/api/admin/member-restrictions/**` | `POST`, `PATCH`, `GET` | 회원 제재 생성/비활성화/조회 | `ADMIN` |

특징:

- 인증 관련 public path는 Gateway에서 화이트리스트로 관리한다.
- authenticated API는 `@CurrentMember` 기반으로 현재 사용자 문맥을 복원한다.
- 관리자 API는 `gateway.auth.role-rules`로 1차 role 검사를 받고, 세부 검증은 서비스 내부에서 수행한다.

---

## 5. 서비스 내부 요청 흐름

대표 흐름:

1. 회원 가입
   - `MemberController`가 회원 가입 요청을 수신한다.
   - `MemberService`가 회원 중복, 비밀번호 인코딩, 초기 role/status를 처리한다.
   - JPA로 `Member`를 저장한다.
   - transaction commit 후 `MemberSignedUpEventListener`가 Kafka 발행을 수행한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as MemberController
       participant Service as MemberService
       participant Repo as MemberRepository
       participant Listener as MemberSignedUpEventListener
       participant Kafka

       Client->>Gateway: POST /api/members
       Gateway->>Controller: Forward request
       Controller->>Service: signUp(command)
       Service->>Service: 중복 확인, 비밀번호 인코딩, 초기 상태 설정
       Service->>Repo: save(Member)
       Repo-->>Service: saved Member
       Service-->>Controller: 회원 가입 결과 반환
       Controller-->>Gateway: 201 Created
       Gateway-->>Client: Response
       Service-->>Listener: AFTER_COMMIT domain event
       Listener->>Kafka: publish MEMBER_SIGNED_UP
   ```

2. 로그인
   - `AuthController`가 로그인 요청과 세션 메타데이터를 받는다.
   - `AuthLoginService`가 회원 조회, 비밀번호 확인, 로그인 가능 상태 검증을 수행한다.
   - `AuthTokenIssuer`가 access/refresh token을 발급한다.
   - Redis에 세션과 refresh token 식별자를 저장한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as AuthController
       participant Service as AuthLoginService
       participant Repo as MemberRepository
       participant Issuer as AuthTokenIssuer
       participant Redis

       Client->>Gateway: POST /api/auth/login
       Gateway->>Controller: Forward request
       Controller->>Service: login(command, sessionMetadata)
       Service->>Repo: findByEmail(...)
       Repo-->>Service: Member
       Service->>Service: 비밀번호 검증, 로그인 가능 상태 확인
       Service->>Issuer: issueTokens(member, sessionMetadata)
       Issuer->>Redis: 세션/refresh token 식별자 저장
       Issuer-->>Service: access token, refresh token
       Service-->>Controller: 로그인 결과 반환
       Controller-->>Gateway: 200 OK
       Gateway-->>Client: Response
   ```

3. 회원 탈퇴
   - `MemberService`가 현재 비밀번호를 검증한다.
   - `MemberWithdrawalCheckFeignAdapter`가 order/payment/product/auction/settlement 상태를 조회한다.
   - 탈퇴 가능 조건을 만족하면 회원 상태를 변경하고 세션/토큰 정리를 진행한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as MemberController
       participant Service as MemberService
       participant Checker as MemberWithdrawalCheckFeignAdapter
       participant Repo as MemberRepository
       participant Redis
       participant Other as Other Services

       Client->>Gateway: DELETE /api/members/me
       Gateway->>Controller: Forward request + current member
       Controller->>Service: withdraw(memberId, password)
       Service->>Service: 현재 비밀번호 검증
       Service->>Checker: checkWithdrawable(memberId)
       Checker->>Other: order/payment/product/auction/settlement 조회
       Other-->>Checker: 잔여 상태 응답
       Checker-->>Service: 탈퇴 가능 여부
       Service->>Repo: 회원 상태 변경 저장
       Service->>Redis: 세션/refresh token 정리
       Service-->>Controller: 탈퇴 완료
       Controller-->>Gateway: 204 No Content
       Gateway-->>Client: Response
   ```

---

## 6. 이벤트 연동

### 6.1 발행 이벤트

| Topic | Event Type | When | Delivery |
|---|---|---|---|
| `member-signed-up` | `MEMBER_SIGNED_UP` | 회원 가입 완료 후 | transaction after-commit direct publish |
| `member-seller-promoted` | `SELLER_PROMOTED` | 판매자 전환 완료 후 | transaction after-commit direct publish |
| `member-account-verification-expired` | `ACCOUNT_VERIFICATION_EXPIRED` | 계좌 인증 세션 만료 처리 후 | transaction after-commit direct publish |
| `member-account-verification-failed` | `ACCOUNT_VERIFICATION_FAILED` | 계좌 인증 실패 확정 후 | transaction after-commit direct publish |
| `member-oauth-linked` | `MEMBER_OAUTH_LINKED` | OAuth 계정 연동 완료 후 | transaction after-commit direct publish |

특징:

- Kafka topic 상수는 `KafkaTopics`에 정의되어 있다.
- 이벤트 payload는 `EventEnvelope`를 사용한다.
- 발행은 `@TransactionalEventListener(phase = AFTER_COMMIT)` 이후 Kafka producer가 수행한다.
- Outbox pattern은 적용되어 있지 않다.

### 6.2 소비 이벤트

현재 코드 기준으로 Member Service의 핵심 도메인 consumer는 없다.

즉 Member Service는 이 문서 범위에서 producer 중심 서비스다.

### 6.3 실패 처리

- outbox 미적용 direct publish 구조이므로 Kafka 발행 실패 시 재시도/유실 대응이 제한적이다.
- 로그인/세션 계열은 Kafka보다 Redis 상태 일관성이 더 중요하다.
- OAuth, 이메일, 계좌 인증은 예외를 서비스 예외로 변환해 HTTP 오류로 반환한다.

---

## 7. 외부 의존성

- PostgreSQL
  - `member` schema 사용
  - 회원, 판매자, 신고, 제재, OAuth 계정, 이메일 인증 상태 저장
- Redis
  - 세션, blacklist, refresh token, 비밀번호 재설정 토큰, OAuth state, 계좌 인증 세션 저장
- Kafka
  - 회원/판매자/계좌 인증/OAuth 연동 이벤트 발행
- SMTP 또는 logging email provider
  - 이메일 인증, 비밀번호 재설정 메일 발송
- AWS S3
  - 프로필 이미지 presigned URL 생성
- Kakao OAuth API
  - 카카오 로그인/연동
- Internal service HTTP calls
  - `order`, `payment`, `product`, `auction`, `settlement`
  - 주로 회원 탈퇴 가능 여부와 판매자 차단 요약 확인

---

## 8. 보안 / 인가

- 인증은 Gateway가 수행한다.
- Member Service는 `X-Member-Id`, `X-Member-Role`, `X-Session-Id`를 `AuthenticatedMember`로 복원한다.
- public endpoint:
  - 회원 가입
  - 로그인 / refresh
  - 비밀번호 재설정
  - 이메일 인증
  - 카카오 OAuth callback/result
  - 프로필 이미지 presign
- authenticated endpoint:
  - 내 정보 조회/수정/탈퇴
  - 세션 관리
  - OAuth 연동 해제
  - 계좌 인증
  - 판매자 등록
  - 신고 생성/조회
- admin endpoint:
  - 신고 검토
  - 회원 제재 생성/비활성화/조회

주의점:

- Gateway의 role-rule이 1차 접근 제어를 담당하지만, 탈퇴 가능 여부나 OAuth unlink 가능 여부 같은 세부 규칙은 Member Service가 검증한다.
- 회원 탈퇴, 판매자 전환, 제재 활성 상태는 로그인 가능성과 후속 인증 흐름에 영향을 준다.

---

## 9. 트랜잭션 / 일관성

주요 transaction boundary:

- `MemberService`
- `AuthLoginService` 일부 상태 저장
- `AuthTokenRefreshService`
- `PasswordResetService`
- `KakaoOAuthService`
- `SellerService`, `SellerPromotionService`
- `EmailVerificationService`
- `AccountVerificationService`
- `MemberReportService`
- `MemberRestrictionService`

일관성 특성:

- DB 상태 변경 후 Kafka 발행은 `AFTER_COMMIT` 이벤트 리스너로 분리된다.
- 따라서 DB commit 이후 Kafka 발행 실패 가능성이 남아 있다.
- 인증 세션과 blacklist는 Redis를 원본 상태로 사용하므로, 일부 흐름은 PostgreSQL보다 Redis 일관성이 더 중요하다.
- 회원 탈퇴 가능 여부는 여러 서비스의 요약 상태를 조회해 결정하므로, 완전한 단일 DB transaction이 아니다.

---

## 10. 운영 메모

- 별도 scheduler는 현재 확인되지 않는다.
- 모니터링은 `common-monitoring` 기반 Actuator/Prometheus endpoint를 사용한다.
- 로그는 기본 Spring Boot stdout 로그 중심이다.
- 로그인/refresh/logout 문제는 Redis 상태와 JWT claim 일치 여부를 먼저 확인해야 한다.
- OAuth 문제는 Kakao callback, Redis authorize state, pending link TTL을 함께 봐야 한다.
- 판매자 등록/탈퇴 문제는 내부 Feign 연동 서비스 응답과 계좌 인증 세션 상태를 함께 확인해야 한다.

주요 위험:

- direct publish 이벤트 유실 가능성
- Redis 장애 시 세션/refresh/blacklist 검증 실패
- 외부 메일/OAuth/S3 실패 시 사용자 인증 보조 흐름 중단
- 내부 서비스 요약 조회 실패 시 회원 탈퇴 차단 또는 오류 응답

---

## 11. 관련 파일

- `service/member/src/main/java/com/example/member/auth/**`
- `service/member/src/main/java/com/example/member/member/**`
- `service/member/src/main/java/com/example/member/seller/**`
- `service/member/src/main/java/com/example/member/verification/**`
- `service/member/src/main/java/com/example/member/report/**`
- `service/member/src/main/java/com/example/member/restriction/**`
- `service/member/src/main/resources/application.yml`

---

## 12. 관련 문서

- [02-architecture.md](../02-architecture.md)
- [04-request-flow.md](../04-request-flow.md)
- [05-event-strategy.md](../05-event-strategy.md)
- [06-auth-flow.md](../06-auth-flow.md)
- [08-deployment.md](../08-deployment.md)
