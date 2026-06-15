# Environment Variables Guide

## Table of Contents

- [1. 개요](#1-개요)
- [2. 파일 위치와 로딩 순서](#2-파일-위치와-로딩-순서)
- [3. 로컬 개발](#3-로컬-개발)
- [4. 변수 그룹](#4-변수-그룹)
  - [4.1 공통 - 데이터베이스](#41-공통---데이터베이스)
  - [4.2 공통 - Kafka와 Redis](#42-공통---kafka와-redis)
  - [4.3 공통 - 인증과 보안](#43-공통---인증과-보안)
  - [4.4 결제 연동](#44-결제-연동)
  - [4.5 AI](#45-ai)
  - [4.6 이메일](#46-이메일)
  - [4.7 OAuth](#47-oauth)
  - [4.8 오브젝트 스토리지 S3](#48-오브젝트-스토리지-s3)
  - [4.9 프론트엔드 콜백 URL](#49-프론트엔드-콜백-url)
  - [4.10 내부 서비스 URL](#410-내부-서비스-url)
- [5. 참고사항](#5-참고사항)
- [6. 관련 문서](#6-관련-문서)

---

## 1. 개요

이 문서는 GoodsMall 로컬 실행과 운영 배포에서 사용하는 환경 변수를 정리한다.

현재 프로젝트는 대부분의 서비스를 `application.yml` 하나로 유지하고, 환경 차이는 아래 방식으로 처리한다.

- `${ENV_NAME:default}` 형태의 기본값
- `.env` 또는 실행 환경에서 주입하는 환경 변수

표의 `필수` 기준은 **이 프로젝트를 처음 받는 개발자가 로컬에서 해당 기능을 실제로 사용해보는 경우**다.

- `✅` 필수: 해당 기능 그룹을 실제로 사용하려면 필요
- `⚠️` 조건부 필수: 특정 외부 연동이나 세부 기능 확인 시 필요
- `❌` 선택: 값이 없어도 기본값으로 대표 기능 사용 가능

---

## 2. 파일 위치와 로딩 순서

기본 기준 파일:

- root infra: [`.env.infra.example`](/C:/my_project/GoodsMall_BE/.env.infra.example)
- root compose: [`.env.example`](/C:/my_project/GoodsMall_BE/.env.example)
- local runtime: `.env`

로컬 실행 전에는 보통 아래 명령으로 준비한다.

```bash
cp .env.infra.example .env
```

대부분 서비스는 root `.env`를 기준으로 Docker Compose 또는 실행 환경에서 값을 읽는다.

---

## 3. 로컬 개발

로컬 개발 기준은 다음과 같다.

1. `.env.infra.example`을 `.env`로 복사한다.
2. `docker-compose.infra.yml`로 PostgreSQL, Kafka, Redis, Elasticsearch, `db-migration`, Prometheus, Grafana를 실행한다.
3. 각 서비스는 `bootRun`으로 실행한다.

로컬 `bootRun` 기본값:

- PostgreSQL: `localhost:5432`
- Kafka: `localhost:29092`
- Redis: `localhost:6379`
- Elasticsearch: `localhost:9200`

즉 `.env`의 값을 모두 채우지 않아도 많은 서비스는 로컬 기본값만으로 시작할 수 있다.

---

## 4. 변수 그룹

### 4.1 공통 - 데이터베이스

> 적용 서비스: `member`, `product`, `cart`, `auction`, `order`, `payment`, `settlement`, `notification`, `ai`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `DB_URL` | ✅ | 로컬 `jdbc:postgresql://localhost:5432/goods_mall`, infra compose `jdbc:postgresql://postgres:5432/goods_mall` | 서비스 datasource JDBC URL |
| `DB_USER_NAME` | ✅ | 로컬 `goods`, infra compose `goods` | 애플리케이션 DB 접속 계정 |
| `DB_USER_PASSWORD` | ✅ | 로컬 `change-me`, infra compose `change-me` | 애플리케이션 DB 접속 비밀번호 |
| `DB_SEED_ENABLED` | ❌ | `false` | `db-migration` seed 실행 여부 |
| `DB_SEED_LOCATIONS` | ❌ | `—` | seed SQL 위치 목록 |

### 4.2 공통 - Kafka와 Redis

> 적용 서비스: Kafka - `member`, `product`, `cart`, `auction`, `order`, `payment`, `settlement`, `notification`, `ai` / Redis - `gateway`, `member`, `payment`, `ai`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | ✅ | 로컬 `localhost:29092`, infra compose `kafka:9092` | Kafka broker 접속 주소 |
| `REDIS_HOST` | ✅ | 로컬 `localhost`, infra compose `redis` | Redis 호스트 |
| `REDIS_PORT` | ✅ | `6379` | Redis 포트 |

### 4.3 공통 - 인증과 보안

> 적용 서비스: `gateway`, `member`, `payment`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `JWT_SECRET_KEY` | ✅ | `—` | JWT 서명 키 |
| `ACCOUNT_VERIFICATION_SECRET_KEY` | ⚠️ | `local-dev-account-verification-secret-key` | member 계정 인증 토큰 보호 키 |
| `PAYMENT_WITHDRAW_CRYPTO_SECRET_KEY` | ⚠️ | `local-dev-payment-withdraw-secret-key` | payment 출금 보호 키 |

### 4.4 결제 연동

> 적용 서비스: `payment`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `TOSS_PAYMENTS_CLIENT_KEY` | ✅ | `test_gck_your_client_key` | Toss client key |
| `TOSS_PAYMENTS_SECRET_KEY` | ✅ | `test_gsk_your_secret_key` | Toss secret key |

### 4.5 AI

> 적용 서비스: `ai`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `OPENAI_API_KEY` | ⚠️ | `sk-replace-me` | OpenAI API 키 |

### 4.6 이메일

> 적용 서비스: `member`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `EMAIL_PROVIDER` | ❌ | `logging` | `smtp` 또는 `logging`. 기본값은 실제 SMTP 전송 대신 로그 출력 |
| `SMTP_HOST` | ⚠️ | `localhost` | SMTP 호스트 |
| `SMTP_PORT` | ⚠️ | `1025` | SMTP 포트 |
| `SMTP_USERNAME` | ⚠️ | `—` | SMTP 사용자명 |
| `SMTP_PASSWORD` | ⚠️ | `—` | SMTP 비밀번호 |
| `MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION` | ❌ | `false` | 회원가입 시 이메일 검증 강제 여부 |

### 4.7 OAuth

> 적용 서비스: `member`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `KAKAO_CLIENT_ID` | ⚠️ | `—` | Kakao OAuth client id |
| `KAKAO_CLIENT_SECRET` | ⚠️ | `—` | Kakao OAuth client secret |

### 4.8 오브젝트 스토리지 S3

> 적용 서비스: `member`, `product`, `auction`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `AWS_ACCESS_KEY_ID` | ⚠️ | `—` | member 서비스 S3 access key |
| `AWS_SECRET_ACCESS_KEY` | ⚠️ | `—` | member 서비스 S3 secret key |
| `AWS_ACCESS_KEY` | ⚠️ | `—` | product / auction 서비스 S3 access key |
| `AWS_SECRET_KEY` | ⚠️ | `test` | product / auction 서비스 S3 secret key |

### 4.9 프론트엔드 콜백 URL

> 적용 서비스: `member`, `payment`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `FRONTEND_BASE_URL` | ❌ | `http://localhost:5173` | 프론트엔드 기본 호스트. 회원 인증 링크, Kakao callback, Toss success/fail 기본값 계산에 사용 |

### 4.10 내부 서비스 URL

> 적용 서비스: `member`, `order`, `auction`, `payment`, `ai`

| 변수 | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `API_GATEWAY_HOST` | ❌ | `http://localhost:8080` | member가 참조하는 gateway base URL |
| `SERVICES_ORDER_URL` | ❌ | `http://localhost:8084` | member / payment가 참조하는 order 서비스 URL |
| `SERVICES_PAYMENT_URL` | ❌ | `http://localhost:8082` | member / order / auction이 참조하는 payment 서비스 URL |
| `SERVICES_PRODUCT_URL` | ❌ | `http://localhost:8081` | member가 참조하는 product 서비스 URL |
| `AI_PRODUCT_API_BASE_URL` | ❌ | `http://localhost:8081` | ai가 참조하는 product 서비스 URL |
| `SERVICES_AUCTION_URL` | ❌ | `http://localhost:8090` | member가 참조하는 auction 서비스 URL |
| `SERVICES_SETTLEMENT_URL` | ❌ | `http://localhost:8085` | member가 참조하는 settlement 서비스 URL |

---

## 5. 참고사항

- `.env.infra.example`은 로컬 infra 실행과 `bootRun` 기준의 기본 예시다.
- `.env.example`은 전체 `docker-compose.yml` 실행 기준의 예시다.
- 운영 환경에서는 Kubernetes `ConfigMap`과 `Secret`으로 값을 주입한다.
- `member`는 현재 `EMAIL_PROVIDER=logging` 기본값을 사용하므로 별도 설정이 없으면 메일은 로그로만 남는다.
- `MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION=false`가 기본값이므로 로컬에서는 이메일 검증 없이 회원가입 흐름을 확인할 수 있다.
- 내부 서비스 URL(`SERVICES_*_URL`)은 로컬 `localhost` 주소와 Kubernetes 서비스 DNS가 다르므로 환경 변수로 유지한다.
- 프론트엔드 관련 redirect / callback 경로는 `FRONTEND_BASE_URL` 하나만 환경 변수로 받고, 세부 경로는 각 서비스 설정 파일에서 파생한다.
- Kakao callback API URL도 `KAKAO_REDIRECT_URI`를 별도로 받지 않고 `API_GATEWAY_HOST`에서 파생한다.
- `SMTP_AUTH`, `SMTP_STARTTLS_ENABLE`, Kakao OAuth 고정 endpoint, Kakao OAuth TTL, Toss widget 사용 여부, AI 모델명과 AI rerank 기본 파라미터는 각 서비스의 `application.yml` 기본값으로 관리한다.
- `payment`, `settlement`의 Kafka topic 이름, consumer group, retry / DLQ 파라미터는 환경 변수가 아니라 코드 상수로 관리한다.

---

## 6. 관련 문서

- [04-request-flow.md](04-request-flow.md)
- [06-auth-flow.md](06-auth-flow.md)
- [08-deployment.md](08-deployment.md)
- [09-engineering-decisions.md](09-engineering-decisions.md)
- [service/member-service.md](service/member-service.md)
