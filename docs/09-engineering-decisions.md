# 설계 결정

## 목차

- [1. 개요](#1-개요)
- [2. 왜 MSA를 선택했는가](#2-왜-msa를-선택했는가)
- [3. 왜 비동기 흐름에 Kafka를 사용했는가](#3-왜-비동기-흐름에-kafka를-사용했는가)
- [4. 왜 인증을 Gateway에서 중앙화했는가](#4-왜-인증을-gateway에서-중앙화했는가)
- [5. 왜 Redis를 사용했는가](#5-왜-redis를-사용했는가)
- [6. 왜 정산을 별도 서비스로 분리했는가](#6-왜-정산을-별도-서비스로-분리했는가)
- [7. 왜 환경변수 구성을 단순화했는가](#7-왜-환경변수-구성을-단순화했는가)
- [8. 포트폴리오 초안: Gateway / Member / Notification 설계 구조](#8-포트폴리오-초안-gateway--member--notification-설계-구조)
- [9. 재검토 조건](#9-재검토-조건)
- [10. 관련 문서](#10-관련-문서)

<br>

---

## 1. 개요

이 문서는 GoodsMall에서 중요한 설계 결정을 어떤 문제와 trade-off를 기준으로 내렸는지 정리한다.

목적은 세 가지다.

- 주요 기술 선택의 이유를 코드 밖에 남긴다.
- 당시 고려했던 대안과 현재 선택의 장단점을 기록한다.
- 나중에 구조를 다시 바꿔야 할 때 재검토 기준점을 남긴다.

이 문서는 기술 소개 문서가 아니라 판단 기록 문서다. 따라서 각 항목은 아래 질문에 답하는 형태로 정리한다.

- 어떤 문제가 있었는가
- 어떤 대안이 있었는가
- 왜 이 선택을 했는가
- 무엇을 얻고 무엇을 포기했는가

<br>

---

## 2. 왜 MSA를 선택했는가

### 문제

GoodsMall은 일반 구매, 경매, 결제, 정산, 알림, 추천을 함께 다루는 서비스다. 각 도메인은 상태 변화 속도와 운영 방식이 다르고, 장애가 전파되는 방식도 다르다.

하나의 애플리케이션으로 묶으면 아래 문제가 생긴다.

- 주문, 결제, 정산, 경매의 변경이 서로 강하게 결합된다.
- 특정 도메인의 장애가 전체 서비스 응답에 영향을 주기 쉽다.
- 경매, 알림, AI처럼 운영 특성이 다른 기능을 같은 배포 단위로 다뤄야 한다.

### 선택지

- 모놀리식 구조로 유지
- 도메인별 서비스 분리

### 결정

도메인 책임 기준으로 서비스를 분리했다.

- `gateway`
- `member`
- `product`
- `cart`
- `auction`
- `order`
- `payment`
- `settlement`
- `notification`
- `ai`

### Trade-offs

얻은 것:

- 도메인별 책임 경계가 명확해졌다.
- 장애 영향 범위를 줄일 수 있었다.
- 경매, 정산, AI 같은 영역을 독립적으로 배포하고 조정할 수 있다.

감수한 것:

- 서비스 간 호출과 이벤트 흐름 관리가 필요해졌다.
- 환경 변수, 배포 리소스, 문서화 비용이 증가했다.
- eventual consistency를 수용해야 하는 지점이 생겼다.

<br>

---

## 3. 왜 비동기 흐름에 Kafka를 사용했는가

### 문제

GoodsMall의 핵심 거래 흐름은 요청-응답 한 번으로 끝나지 않는다.

대표적인 후속 처리:

- 입찰 보증금 차감
- 낙찰 주문 생성
- 구매 확정 이후 escrow release
- 정산 후보 생성
- seller 지급 요청
- 알림 생성

이 흐름을 모두 동기 HTTP 호출로 연결하면, 앞 단계 요청이 뒤 단계 장애에 직접 영향을 받게 된다.

### 선택지

- 서비스 간 동기 HTTP 체인 유지
- 후속 처리를 이벤트 기반 비동기 흐름으로 분리

### 결정

후속 처리가 많은 거래 흐름은 Kafka 기반 이벤트로 분리했다. 서비스는 자신의 상태를 저장한 뒤 필요한 이벤트를 발행하고, 다음 서비스가 이를 소비해 후속 작업을 이어간다.

이 구조는 `auction.won`, `order.purchase-confirmed`, `payment.settlement-candidate-created` 같은 흐름에서 사용된다.

### Trade-offs

얻은 것:

- 사용자 응답과 후속 처리를 분리할 수 있다.
- 서비스 간 결합도를 낮출 수 있다.
- 재처리, DLQ, 운영 추적 같은 후속 운영 수단을 적용할 수 있다.

감수한 것:

- 즉시 일관성보다 eventual consistency를 받아들여야 한다.
- 이벤트 계약, 멱등성, 재처리 전략을 별도로 관리해야 한다.
- 디버깅 경로가 HTTP 체인보다 복잡해진다.

<br>

---

## 4. 왜 인증을 Gateway에서 중앙화했는가

### 문제

모든 서비스가 JWT 검증을 각자 수행하면 인증 로직이 중복되고, 토큰 정책이 서비스마다 달라질 위험이 커진다. 반대로 모든 권한 검증까지 Gateway에 몰아넣으면 도메인 규칙이 Gateway에 새어 나온다.

### 선택지

- 모든 서비스가 JWT를 직접 검증
- Gateway에서 인증만 중앙화하고, 도메인 권한은 각 서비스에서 검증

### 결정

JWT 검증과 세션/blacklist 조회는 Gateway에서 중앙화했다. 대신 실제 비즈니스 권한 검증은 각 서비스에 남겼다.

정리하면:

- Gateway: 인증, 공개 경로 판별, 내부 인증 헤더 전달
- 각 서비스: 도메인 소유권, 역할, 상태 기반 권한 검증

### Trade-offs

얻은 것:

- 인증 정책을 한 곳에서 관리할 수 있다.
- 서비스별 인증 중복 코드를 줄일 수 있다.
- 내부 서비스는 공통 헤더 기반으로 사용자 문맥을 일관되게 받는다.

감수한 것:

- Gateway 장애나 설정 실수가 전체 진입점에 영향을 줄 수 있다.
- 인증과 권한의 책임 경계를 문서로 명확히 유지해야 한다.

<br>

---

## 5. 왜 Redis를 사용했는가

### 문제

프로젝트에는 영속 저장보다 빠른 조회와 TTL 기반 만료가 더 중요한 데이터가 있다. 이를 모두 DB에만 두면 인증, 캐시, 멱등성 처리가 비싸진다.

### 결정

Redis를 아래 용도로 사용한다.

- refresh token / session 저장
- access token / session blacklist 조회
- AI 추천 캐시
- 이벤트 멱등 키
- draft assist lock / 결과 캐시

### 왜 데이터베이스만으로 처리하지 않았는가

- TTL 기반 만료를 자연스럽게 처리하기 어렵다.
- Gateway에서 인증 상태를 빠르게 조회해야 한다.
- 캐시와 lock, 짧은 생명주기의 키-값 상태에 더 적합하다.

### Trade-offs

얻은 것:

- 인증과 캐시 조회 성능을 확보할 수 있다.
- 만료 정책을 단순하게 구현할 수 있다.
- 멱등성과 임시 상태 관리를 분리할 수 있다.

감수한 것:

- 저장소가 하나 더 늘어난다.
- Redis 장애 시 인증/캐시 관련 영향이 생긴다.

<br>

---

## 6. 왜 정산을 별도 서비스로 분리했는가

### 문제

정산은 결제와 가까워 보이지만, 실제 흐름은 다르다.

- 구매 확정 이후에만 정산 가능
- 월별 정산, 부분 정산, seller 지급 요청 필요
- 지급 결과 반영과 재시도 필요

즉 결제 성공과 정산 완료는 같은 시점의 문제가 아니다.

### 선택지

- `payment` 안에 정산 로직 포함
- 별도 `settlement` 서비스로 분리

### 결정

정산은 `settlement` 서비스와 배치/이벤트 흐름으로 분리했다.

- `payment`는 정산 후보 생성 이벤트를 발행
- `settlement`는 후보를 적재하고 월별/부분 정산 단위로 집계
- 지급 요청과 결과 반영은 별도 흐름으로 처리

### Trade-offs

얻은 것:

- 결제와 정산의 관심사를 분리할 수 있다.
- 집계, 지급 요청, 실패 재시도 같은 운영 로직을 정산 경계 안에 둘 수 있다.
- seller 지급 흐름을 독립적으로 조정할 수 있다.

감수한 것:

- 서비스 수와 이벤트 흐름이 늘어난다.
- 운영자가 확인해야 할 상태 저장소가 많아진다.

<br>

---

## 7. 왜 환경변수 구성을 단순화했는가

### 문제

초기에는 secret, 접속 정보, 정책값, 고정 endpoint, 튜닝값이 모두 env로 섞여 있었다. 이 상태에서는 아래 문제가 생긴다.

- 실제로 운영자가 조정해야 할 값과 코드가 책임져야 할 값이 구분되지 않는다.
- `.env.example`, ConfigMap, 문서의 길이가 길어진다.
- 더 이상 읽지 않는 stale env가 남기 쉽다.

### 결정

환경 변수는 아래처럼 다시 분류했다.

- Secret: Secret으로 유지
- 접속 정보와 환경별 URL: env 유지
- 고정 endpoint, 정책 기본값, 모델명, 일부 튜닝값: 설정 파일로 회수

대표 예:

- 설정 파일로 회수
  - Kakao endpoint
  - Toss base URL
  - S3 bucket / region
  - AI 모델 기본값
  - MAIL_FROM_NAME
- env 유지
  - DB URL / 계정
  - Kafka / Redis host
  - `SERVICES_*_URL`
  - `FRONTEND_BASE_URL`
  - JWT / AWS / Kakao / Toss / OpenAI secret 계열

### Trade-offs

얻은 것:

- 운영자가 실제로 조정해야 할 값이 줄어든다.
- 문서와 설정 구조가 단순해진다.
- stale env를 제거하기 쉬워진다.

감수한 것:

- 정책값을 바꾸려면 재배포가 필요한 항목이 생긴다.
- 어떤 값은 “운영자가 바꿀 설정”이 아니라 “코드의 기본 정책”으로 더 명확히 합의해야 한다.

<br>

---

## 8. 포트폴리오 초안: Gateway / Member / Notification 설계 구조

### 역할 요약

Gateway, Member, Notification 영역을 담당했다. 단순히 API를 구현하는 것보다 인증 책임을 어디에 둘지, JWT 기반 인증에서 서버 측 상태를 어떻게 다룰지, 여러 도메인에서 발생하는 이벤트를 사용자 알림으로 어떻게 분리할지에 집중했다.

핵심 설계 주제는 세 가지다.

1. Gateway에서 인증/인가의 1차 책임을 중앙화한 이유
2. JWT 기반 인증에 Redis 세션 상태를 함께 둔 이유
3. 알림을 이벤트 소비 전용 서비스로 분리한 이유

---

### 8.1 Gateway에서 인증 책임을 중앙화한 이유

#### 문제

MSA 구조에서는 모든 서비스가 외부 요청을 받을 수 있다. 각 서비스가 JWT 검증, public path 판별, role 검사, blacklist 조회를 직접 처리하면 같은 인증 정책이 여러 코드베이스에 흩어진다.

이 방식은 아래 위험이 있다.

- 서비스마다 JWT 검증 방식이 달라질 수 있다.
- public path와 role rule 변경 시 여러 서비스를 함께 수정해야 한다.
- 로그아웃이나 전체 세션 종료처럼 blacklist가 필요한 정책을 모든 서비스가 반복 구현해야 한다.

반대로 Gateway가 모든 권한 판단까지 가져가면, 주문 소유권, 알림 소유권, 판매자 상태 같은 도메인 규칙이 Gateway로 새어 나온다.

#### 선택한 구조

Gateway는 인증과 1차 접근 제어만 담당하도록 했다.

- JWT access token 검증
- Redis 기반 access token / session blacklist 조회
- public path, public rule, role rule 기반 1차 접근 제어
- 검증된 사용자 정보를 `X-Member-Id`, `X-Member-Role`, `X-Session-Id`로 downstream service에 전달

각 서비스는 Gateway가 전달한 사용자 컨텍스트를 신뢰하되, 리소스 소유권과 상태 기반 권한은 직접 판단한다.

예:

- Gateway: `ADMIN`만 관리자 신고 API에 접근 가능하도록 1차 차단
- Member: 특정 회원 신고를 승인할 수 있는지, 제재 상태를 어떻게 바꿀지 판단
- Notification: 현재 사용자가 본인 알림만 읽을 수 있도록 소유권 검증

#### Trade-offs

얻은 것:

- 인증 정책을 Gateway 설정과 필터 중심으로 관리할 수 있다.
- downstream service는 JWT 파싱보다 도메인 규칙에 집중할 수 있다.
- 사용자 컨텍스트 전달 방식이 공통 헤더로 통일된다.

감수한 것:

- Gateway 설정 오류가 전체 API 접근에 영향을 줄 수 있다.
- Gateway의 role rule과 각 서비스의 세부 권한 검증 경계를 계속 문서화해야 한다.
- 내부 서비스 간 호출에서 인증 헤더 신뢰 경계가 명확해야 한다.

---

### 8.2 JWT 기반 인증에 Redis 세션 상태를 함께 둔 이유

#### 문제

JWT는 stateless하다는 장점이 있지만, 실제 서비스 인증 요구사항은 완전히 stateless하게 처리하기 어렵다.

GoodsMall에는 아래 요구가 있다.

- 현재 세션 로그아웃
- 전체 세션 로그아웃
- refresh token rotation
- access token 강제 무효화
- session 단위 차단
- OAuth state, 비밀번호 재설정 토큰, 이메일 인증 자동 로그인 토큰 같은 짧은 생명주기 상태 관리

이 요구를 JWT만으로 처리하면 토큰 만료 전까지 무효화를 반영하기 어렵다. 모든 상태를 PostgreSQL에 저장하면 TTL 기반 만료와 Gateway의 빠른 조회에 부담이 생긴다.

#### 선택한 구조

Member Service가 인증 상태의 원본을 관리하고, Gateway가 요청 시 필요한 blacklist 상태를 Redis에서 조회하도록 분리했다.

- Member Service
  - 로그인 시 session과 refresh token 식별자 저장
  - refresh token rotation 처리
  - 로그아웃 시 access token / session blacklist 기록
  - OAuth, 비밀번호 재설정, 이메일/계좌 인증의 임시 상태 관리

- Gateway Service
  - access token 검증
  - access token ID와 session ID 기준 blacklist 조회
  - 유효한 요청에만 사용자 컨텍스트 헤더 전달

Redis는 세션/토큰/임시 인증 상태처럼 TTL이 중요하고 조회가 잦은 데이터를 담당하고, PostgreSQL은 회원, 판매자, 신고, 제재, OAuth 계정 같은 영속 도메인 상태를 담당한다.

#### Trade-offs

얻은 것:

- JWT 기반 인증에서도 로그아웃과 세션 종료를 서버 측에서 반영할 수 있다.
- TTL 기반 임시 인증 상태를 단순하게 관리할 수 있다.
- Gateway가 DB를 직접 보지 않고도 빠르게 blacklist를 확인할 수 있다.

감수한 것:

- Redis 장애가 인증 흐름에 직접 영향을 줄 수 있다.
- JWT claim, Redis session, refresh token 상태가 함께 맞아야 하므로 운영 확인 지점이 늘어난다.
- access token 자체는 여전히 만료 시간까지 존재하므로 blacklist 정책을 신중히 유지해야 한다.

---

### 8.3 알림을 이벤트 소비 전용 서비스로 분리한 이유

#### 문제

알림은 주문, 결제, 회원, 경매, 정산 등 여러 도메인 상태 변화에서 발생한다. 각 서비스가 직접 알림을 저장하거나 SSE로 전송하면, 사용자 알림 정책이 여러 서비스에 흩어진다.

이 방식은 아래 문제가 있다.

- 알림 메시지 형식과 읽음 상태 관리가 서비스마다 달라질 수 있다.
- SSE 연결 관리가 여러 서비스에 중복된다.
- 어떤 이벤트가 어떤 알림으로 변환되는지 추적하기 어렵다.
- 이벤트 중복 수신이나 실패 처리 정책이 분산된다.

#### 선택한 구조

Notification Service를 별도 서비스로 두고, 다른 도메인의 Kafka 이벤트를 사용자 알림으로 변환하도록 했다.

흐름:

1. 주문, 결제, 회원, 경매, 정산 서비스가 도메인 이벤트를 발행한다.
2. Notification Service가 Kafka 이벤트를 소비한다.
3. 공통 consumer가 envelope를 검증하고 `eventType` 기준으로 handler를 선택한다.
4. handler가 알림 command를 만들고 inbox에 저장한다.
5. transaction commit 이후 SSE push를 시도한다.

Notification Service는 원본 도메인 상태를 소유하지 않는다. 대신 사용자에게 보여줄 알림 상태만 소유한다.

소유 상태:

- 알림 inbox
- 읽음 여부
- 알림 처리 상태
- 이벤트 중복 방지 키
- SSE emitter registry

#### Trade-offs

얻은 것:

- 알림 정책과 메시지 생성 규칙을 한 서비스에 모을 수 있다.
- 각 도메인 서비스는 알림 구현을 몰라도 이벤트만 발행하면 된다.
- 알림 조회, 미읽음 개수, 읽음 처리, SSE push를 하나의 도메인으로 관리할 수 있다.
- `eventId + memberId + type` 기준으로 중복 이벤트를 방어할 수 있다.

감수한 것:

- 이벤트 계약이 깨지면 알림 생성도 영향을 받는다.
- Kafka 소비 실패, DLQ, 재처리 정책을 별도로 운영해야 한다.
- SSE registry가 메모리 기반이므로 다중 인스턴스 환경에서는 연결 위치 문제가 생길 수 있다.

### 포트폴리오 서술 초안

GoodsMall에서 Gateway, Member, Notification 영역을 담당했다. 이 영역의 핵심은 인증과 알림 기능 자체보다, 여러 서비스가 공통으로 의존하는 책임을 어디까지 중앙화하고 어디부터 도메인 서비스에 남길지 결정하는 것이었다.

Gateway는 JWT 검증과 Redis blacklist 조회, public path / role rule 기반 1차 접근 제어를 담당하도록 구성했다. 각 서비스가 JWT를 직접 검증하지 않게 해 인증 정책 중복을 줄였고, 검증된 사용자 정보는 `X-Member-Id`, `X-Member-Role`, `X-Session-Id` 헤더로 전달했다. 대신 리소스 소유권과 상태 기반 권한은 각 도메인 서비스가 판단하도록 경계를 나눴다.

Member Service에서는 JWT만으로 처리하기 어려운 로그아웃, 전체 세션 종료, refresh token rotation, OAuth state, 이메일/계좌 인증 임시 상태를 Redis에 분리했다. 영속 회원 상태는 PostgreSQL에 두고, TTL과 빠른 조회가 필요한 인증 보조 상태는 Redis에 두어 Gateway와 Member의 역할을 분리했다.

Notification Service는 주문, 결제, 회원, 경매, 정산 이벤트를 소비해 사용자 알림으로 변환하는 전용 서비스로 설계했다. 각 도메인 서비스가 알림을 직접 만들지 않고 Kafka 이벤트만 발행하게 했고, Notification Service는 eventType별 handler로 메시지를 생성해 inbox에 저장한 뒤 SSE push를 시도한다. 이벤트 중복 수신에 대비해 `eventId + memberId + type` 기준으로 중복 저장을 방지하고, 실패 이벤트는 재시도와 DLQ로 분리했다.

이 구조를 통해 인증 정책, 세션 상태, 알림 생성 책임을 각각 Gateway, Member, Notification으로 나누었다. 기능을 많이 넣는 것보다 서비스가 소유해야 할 상태와 소유하지 말아야 할 책임을 분리하는 데 초점을 맞췄다.

<br>

---

## 9. 재검토 조건

아래 조건이 생기면 현재 결정을 다시 검토할 수 있다.

- 서비스 수 증가로 Gateway 병목이나 운영 복잡도가 커질 때
- Kafka 기반 후속 처리보다 강한 즉시 일관성이 필요한 요구가 늘어날 때
- 정산 로직이 더 커져 별도 저장소나 배치 플랫폼이 필요해질 때
- env 회수한 정책값을 운영 중 자주 조정해야 하는 요구가 반복될 때
- Gateway role rule이 복잡해져 도메인 권한 규칙과 섞이기 시작할 때
- Redis 기반 세션/blacklist 조회가 인증 병목이 되거나 장애 전파 지점이 될 때
- Notification SSE를 다중 인스턴스로 확장해야 해 emitter 위치 문제가 커질 때

<br>

---

## 10. 관련 문서

- [02-architecture.md](02-architecture.md)
- [03-service-responsibilities.md](03-service-responsibilities.md)
- [05-event-strategy.md](05-event-strategy.md)
- [06-auth-flow.md](06-auth-flow.md)
- [07-environment-variables.md](07-environment-variables.md)
- [08-deployment.md](08-deployment.md)
- [service/gateway.md](service/gateway.md)
- [service/member-service.md](service/member-service.md)
- [service/notification-service.md](service/notification-service.md)
- [service/payment-service.md](service/payment-service.md)
- [service/settlement-service.md](service/settlement-service.md)
