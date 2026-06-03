# Verification Package

## 목차

- [1. 책임](#1-책임)
- [2. 도메인 모델](#2-도메인-모델)
- [3. Redis 상태](#3-redis-상태)
- [4. 주요 서비스](#4-주요-서비스)
- [5. 포트](#5-포트)
- [6. 인프라 어댑터](#6-인프라-어댑터)
- [7. 주요 흐름](#7-주요-흐름)
- [8. 관련 파일](#8-관련-파일)

---

## 1. 책임

`verification` 패키지는 회원 인증 보조 흐름을 담당한다.

주요 책임:

- 이메일 인증 메일 발송
- 이메일 인증 확인
- 이메일 인증 후 자동 로그인 토큰 발급
- 계좌 인증 세션 생성
- 계좌 인증 코드 확인
- 계좌 인증 취소
- 계좌 인증 만료/실패 이벤트 발행

---

## 2. 도메인 모델

| 엔티티 / Enum | 설명 |
|---|---|
| `EmailVerification` | 이메일 인증 이력 |
| `EmailVerificationStatus` | 이메일 인증 상태 |
| `EmailVerificationPurpose` | 가입, 비밀번호 재설정 등 인증 목적 |
| `AccountVerificationStatus` | 계좌 인증 상태 |

---

## 3. Redis 상태

| 상태 | 설명 |
|---|---|
| account verification session | 계좌 인증 코드, 시도 횟수, 만료 상태 |
| email verification auto-login token | 이메일 인증 후 자동 로그인 토큰 |

---

## 4. 주요 서비스

| 클래스 | 책임 |
|---|---|
| `EmailVerificationService` | 이메일 인증 발송/확인 |
| `EmailVerificationAutoLoginService` | 이메일 인증 후 자동 로그인 토큰 처리 |
| `AccountVerificationService` | 계좌 인증 생성/조회/확인/취소 |

---

## 5. 포트

| 포트 | 방향 | 설명 |
|---|---|---|
| `AccountVerificationUsecase` | in | 계좌 인증 유스케이스 |
| `EmailSenderPort` | out | 이메일 발송 |
| `EmailVerificationPersistencePort` | out | 이메일 인증 이력 저장소 접근 |
| `EmailVerificationAutoLoginTokenStore` | out | 자동 로그인 토큰 저장 |
| `AccountVerificationEventPort` | out | 계좌 인증 이벤트 발행 |

---

## 6. 인프라 어댑터

| 어댑터 | 설명 |
|---|---|
| `EmailVerificationJpaAdapter` | 이메일 인증 JPA 저장소 adapter |
| `RedisAccountVerificationSessionStore` | 계좌 인증 세션 저장 |
| `RedisEmailVerificationAutoLoginTokenStore` | 자동 로그인 토큰 저장 |
| `SmtpEmailSender` | SMTP 기반 이메일 발송 |
| `LoggingEmailSender` | 로컬/개발용 이메일 발송 대체 |
| `AccountVerificationEventKafkaProducer` | 계좌 인증 이벤트 Kafka 발행 |

---

## 7. 주요 흐름

- 회원가입 전 이메일 인증
- 이메일 인증 후 자동 로그인
- 판매자 전환 전 계좌 인증
- 계좌 인증 실패/만료 이벤트 발행

---

## 8. 관련 파일

- `service/member/src/main/java/com/example/member/verification/**`