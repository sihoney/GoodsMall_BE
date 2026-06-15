# 문제 해결 가이드 (초안)

## 목차

- [1. 개요](#1-개요)
- [2. Docker Infra가 시작되지 않는 경우](#2-docker-infra가-시작되지-않는-경우)
- [3. 로컬 bootRun에서 Kafka 연결이 실패하는 경우](#3-로컬-bootrun에서-kafka-연결이-실패하는-경우)
- [4. ConfigMap을 바꿨는데 동작이 바뀌지 않는 경우](#4-configmap을-바꿨는데-동작이-바뀌지-않는-경우)
- [5. Member 회원가입 이메일 동작이 기대와 다른 경우](#5-member-회원가입-이메일-동작이-기대와-다른-경우)
- [6. Gateway JWT 토글이 동작하지 않는 경우](#6-gateway-jwt-토글이-동작하지-않는-경우)
- [7. 잘못된 예시 env 파일을 사용한 경우](#7-잘못된-예시-env-파일을-사용한-경우)
- [8. 관련 문서](#8-관련-문서)

## 1. 개요

이 문서는 로컬 개발과 운영 설정 정리 과정에서 반복될 가능성이 높은 문제를 빠르게 진단하고 복구하기 위한 가이드다.

각 항목은 아래 순서로 정리한다.

- 증상
- 확인할 것
- 원인
- 해결 방법
- 재발 방지

## 2. Docker Infra가 시작되지 않는 경우

### 증상

- `docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d`가 실패한다.
- Docker API 연결 오류가 나온다.
- `postgres`, `kafka`, `redis` 중 일부 컨테이너가 올라오지 않는다.

### 확인할 것

- Docker Desktop 또는 Docker daemon이 실행 중인지
- 사용한 파일이 `infra/docker/docker-compose.infra.yml`인지
- root `.env`가 존재하는지
- `docker compose --env-file .env -f infra/docker/docker-compose.infra.yml config`가 성공하는지

### 원인

대표 원인은 아래 둘이다.

- Docker daemon이 실행되지 않음
- `.env` 값이 비어 있거나 잘못됨

### 해결 방법

1. Docker Desktop을 실행한다.
2. 아래 명령으로 compose 설정을 먼저 검증한다.

```bash
docker compose --env-file .env -f infra/docker/docker-compose.infra.yml config
```

3. 이후 infra를 다시 실행한다.

```bash
docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
```

### 재발 방지

- 로컬 실행 전에는 Docker daemon 상태를 먼저 확인한다.
- infra 실행은 항상 `docker-compose.infra.yml` 기준으로 문서화된 명령을 사용한다.

## 3. 로컬 bootRun에서 Kafka 연결이 실패하는 경우

### 증상

- `bootRun`한 서비스가 Kafka broker에 연결하지 못한다.
- `Connection to node -1 could not be established` 같은 오류가 나온다.

### 확인할 것

- 서비스의 Kafka 기본값이 `localhost:29092`인지
- infra compose의 Kafka advertised listener가 `localhost:29092`인지
- `docker-compose.yml`이 아니라 `docker-compose.infra.yml`로 띄웠는지

### 원인

로컬 `bootRun`과 컨테이너 내부 주소 기준이 다르다.

- host에서 실행하는 서비스는 `localhost:29092`
- 컨테이너 내부 서비스는 `kafka:9092`

이 기준이 섞이면 Kafka 연결이 실패한다.

### 해결 방법

- 로컬 `bootRun` 기준 `.env`에서는 아래 값을 사용한다.

```env
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:29092
```

- 전체 컨테이너 실행 시에는 `.env.example` 기준 값을 사용한다.

### 재발 방지

- `bootRun`과 전체 compose 실행용 env 예시 파일을 혼용하지 않는다.
- 로컬 실행 문서는 `localhost:29092` 기준으로 유지한다.

## 4. ConfigMap을 바꿨는데 동작이 바뀌지 않는 경우

### 증상

- ConfigMap 값을 바꿨는데 애플리케이션 동작이 변하지 않는다.
- 운영자가 값을 수정했지만 실제 응답이나 로직은 그대로다.

### 확인할 것

- 해당 값이 아직 `application.yml`에서 env로 읽히는지
- 이미 설정 파일 상수로 회수된 값인지
- `infra/k8s/ENV_VARS.md`에 현재 인터페이스로 남아 있는지

### 원인

보통 stale env 때문이다. 예전에는 env였지만, 지금은 설정 파일 상수나 파생값으로 바뀌었는데 ConfigMap만 남아 있는 경우다.

### 해결 방법

1. `application.yml`에서 `${ENV_NAME:...}` 참조가 남아 있는지 확인한다.
2. 남아 있지 않으면 ConfigMap에서 제거한다.
3. `infra/k8s/ENV_VARS.md`도 같이 정리한다.

### 재발 방지

- env를 설정 파일로 회수할 때는
  - `application.yml`
  - `configmap.yaml`
  - `ENV_VARS.md`
  를 같이 정리한다.

## 5. Member 회원가입 이메일 동작이 기대와 다른 경우

### 증상

- 회원가입 시 이메일이 발송되지 않는다.
- 이메일 검증 없이 바로 가입이 완료된다.
- 실제 SMTP 발송 대신 로그만 남는다.

### 확인할 것

- `MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION`
- `EMAIL_PROVIDER`
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`

### 원인

현재 기본 설정은 아래와 같다.

- `MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION=false`
- `EMAIL_PROVIDER=logging`

즉 기본 상태에서는 회원가입 시 이메일 검증 흐름이 꺼져 있고, 메일 provider도 SMTP가 아니라 logging이다.

### 해결 방법

- 회원가입 이메일 검증까지 확인하려면:

```env
MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION=true
EMAIL_PROVIDER=smtp
SMTP_HOST=...
SMTP_PORT=...
SMTP_USERNAME=...
SMTP_PASSWORD=...
```

### 재발 방지

- 이메일 관련 기능을 테스트할 때는 “검증 기능 on/off”와 “발송 provider”를 분리해서 본다.

## 6. Gateway JWT 토글이 동작하지 않는 경우

### 증상

- `GATEWAY_JWT_VALIDATION_ENABLED=false`를 줘도 JWT 검증이 꺼지지 않는다.

### 확인할 것

- `service/gateway/src/main/resources/application.yml`
- `service/gateway/src/main/java/com/todaylunch/gateway/filter/JwtAuthenticationFilter.java`

### 원인

설정 바인딩은 남아 있지만, 필터에서 실제 분기 코드는 주석 처리돼 있다. 따라서 현재 이 값은 dormant 플래그다.

### 해결 방법

- 운영에서는 이 값을 토글 수단으로 사용하지 않는다.
- 정말 토글이 필요하면 필터 분기를 다시 복원해야 한다.

### 재발 방지

- 동작하지 않는 토글은 운영 ConfigMap에 노출하지 않는다.

## 7. 잘못된 예시 env 파일을 사용한 경우

### 증상

- 로컬 `bootRun`인데 `kafka:9092`, `postgres:5432` 같은 컨테이너 내부 주소를 사용한다.
- 반대로 전체 compose 실행인데 `localhost` 기준 값이 들어가 있다.

### 확인할 것

- 복사한 예시 파일이 무엇인지
- 현재 실행 방식이 무엇인지

### 원인

예시 env 파일 용도가 다르다.

- `.env.infra.example`
  - `docker-compose.infra.yml` + `bootRun`
- `.env.example`
  - 전체 `docker-compose.yml`

### 해결 방법

- 로컬 infra + `bootRun`

```bash
cp .env.infra.example .env
docker compose --env-file .env -f infra/docker/docker-compose.infra.yml up -d
./gradlew :{module-name}:bootRun
```

- 전체 compose 실행

```bash
cp .env.example .env
docker compose --env-file .env -f infra/docker/docker-compose.yml up -d --build
```

### 재발 방지

- 실행 방식과 env 예시 파일 이름을 항상 함께 문서화한다.

## 8. 관련 문서

- [07-environment-variables.md](07-environment-variables.md)
- [08-deployment.md](08-deployment.md)
- [09-engineering-decisions.md](09-engineering-decisions.md)
- [service/member-service.md](service/member-service.md)
- [service/gateway.md](service/gateway.md)
