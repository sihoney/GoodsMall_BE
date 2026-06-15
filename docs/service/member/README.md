# Member Service Docs

## 목차

- [1. 개요](#1-개요)
- [2. 패키지 맵](#2-패키지-맵)
- [3. 패키지 구조](#3-패키지-구조)
- [4. 패키지 간 주요 흐름](#4-패키지-간-주요-흐름)
- [5. 서비스 경계 메모](#5-서비스-경계-메모)

---

## 1. 개요

Member Service 상세 문서는 도메인 패키지 기준으로 분리한다.

Member Service는 회원 원본 상태와 인증 보조 상태를 소유한다. Gateway는 JWT 검증과 role rule 기반 1차 접근 제어를 수행하고, Member Service는 탈퇴 가능 여부, OAuth unlink 가능 여부, 판매자 전환 가능 여부 같은 내부 비즈니스 규칙을 최종 검증한다.

---

## 2. 패키지 맵

| 패키지 | 문서 | 책임 |
|---|---|---|
| `member` | [member.md](member.md) | 회원 기본 정보, 회원가입, 프로필, 탈퇴 |
| `auth` | [auth.md](auth.md) | 로그인, 토큰, 세션, OAuth, 비밀번호 재설정 |
| `verification` | [verification.md](verification.md) | 이메일 인증, 계좌 인증 |
| `seller` | [seller.md](seller.md) | 판매자 등록, 판매자 전환, 판매자 등록 draft |
| `report` | [report.md](report.md) | 회원 신고 |
| `restriction` | [restriction.md](restriction.md) | 회원 제재 |
| `common` | [common.md](common.md) | 공통 설정, 응답, 예외, 인증 컨텍스트 |

---

## 3. 패키지 구조

```text
com.example.member
|-- member
|-- auth
|-- verification
|-- seller
|-- report
|-- restriction
`-- common
```

각 도메인 패키지는 대체로 다음 계층을 따른다.

```text
{domain}
|-- application
|   |-- dto
|   |-- event
|   |-- port
|   |   |-- in
|   |   `-- out
|   `-- service
|-- domain
|   |-- entity
|   `-- enumtype
|-- infrastructure
|   |-- persistence
|   |-- messaging
|   |-- redis
|   `-- client/storage/email/crypto
`-- presentation
    `-- web
```

---

## 4. 패키지 간 주요 흐름

### 4.1 회원가입

```text
member.MemberController
-> member.MemberService
-> verification.EmailVerificationService
-> member.MemberSignedUpEventListener
-> Kafka MEMBER_SIGNED_UP
```

### 4.2 로그인

```text
auth.AuthController
-> auth.AuthLoginService
-> member.MemberPersistencePort
-> restriction.MemberRestrictionService
-> auth.AuthTokenIssuer
-> Redis session / refresh token 저장
```

### 4.3 판매자 전환

```text
seller.SellerController
-> seller.SellerService
-> verification.AccountVerificationService
-> seller.SellerPromotionService
-> member.Member role 변경
-> Kafka SELLER_PROMOTED
```

### 4.4 회원 탈퇴

```text
member.MemberController
-> member.MemberService
-> member.MemberWithdrawalCheckPort
-> order/payment/product/auction/settlement 조회
-> member.Member status 변경
-> auth session / token 정리
```

---

## 5. 서비스 경계 메모

- Member Service는 회원 프로필, 역할, 인증/검증 보조 상태를 소유한다.
- Payment Service는 wallet, 현재 예치금 잔액, 거래 이력을 소유한다.
- Member Service는 예치금 잔액을 저장하지 않는다.
- 판매자 등록용 계좌 인증 정보는 판매자 전환 전 임시 상태로만 Member Service에서 관리한다.