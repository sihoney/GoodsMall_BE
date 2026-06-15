# Notification Service

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

Notification Service는 다른 도메인 서비스가 발행한 이벤트를 사용자 알림으로 변환하고, 이를 저장하고, 필요 시 SSE로 즉시 전달하는 서비스다.

핵심 책임:

- Kafka 도메인 이벤트 소비
- 이벤트별 알림 메시지 생성
- 알림 inbox 저장과 중복 방지
- 내 알림 목록 조회, 미읽음 개수 조회, 읽음 처리
- SSE 기반 실시간 알림 구독과 전송
- 소비 실패 분류, 재시도, DLQ 발행

이 서비스는 주문, 결제, 회원, 경매, 정산 도메인의 원본 상태를 소유하지 않는다. 사용자에게 보여줄 알림 상태만 독립적으로 소유한다.

---

## 2. 소유 도메인 / 데이터

주요 영속 데이터:

- `Notification`

`Notification`은 다음 정보를 저장한다.

- `notificationId`
- `eventId`
- `traceId`
- `memberId`
- `type`
- `title`
- `content`
- `referenceId`
- `referenceType`
- `status`
- `isRead`
- `createdAt`
- `statusChangedAt`

주요 상태 규칙:

- 알림 중복 방지는 `(event_id, member_id, type)` 유니크 인덱스로 처리한다.
- 상태 전이는 `RECEIVED -> STORED -> PUSHED` 또는 `FAILED / RETRYING` 흐름을 가진다.
- SSE 연결 정보는 `NotificationSseEmitterRegistry`의 메모리 맵에만 유지되며, 영속 저장하지 않는다.

---

## 3. 주요 유스케이스

- 회원 가입 완료, 판매자 전환, 계좌 인증 상태 변경 알림 생성
- 주문 생성/취소, 결제 성공/실패, 자동 구매 확정 알림 생성
- 정산 지급 성공/실패 알림 생성
- 경매 낙찰, 유찰, 상회 입찰 알림 생성
- 내 알림 목록 조회와 미읽음 개수 조회
- 특정 알림 읽음 처리
- SSE 스트림 구독 후 실시간 알림 수신

---

## 4. API 표면

주요 외부 API:

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/notifications` | `GET` | 내 알림 목록 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/notifications/unread-count` | `GET` | 미읽음 알림 개수 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/notifications/{notificationId}/read` | `PATCH` | 알림 읽음 처리 | `USER`, `SELLER`, `ADMIN` |
| `/api/notifications/stream` | `GET` | SSE 알림 스트림 구독 | `USER`, `SELLER`, `ADMIN` |

특징:

- 현재 공개 API는 없고, 모든 API는 Gateway의 role rule 뒤에서 동작한다.
- `@CurrentMember`를 사용해 Gateway가 전달한 사용자 문맥을 복원한다.
- SSE 구독도 동일한 인증 흐름을 통과한 뒤 연결된다.

---

## 5. 서비스 내부 요청 흐름

대표 흐름:

1. 이벤트 기반 알림 생성
   - `NotificationEventConsumer`가 Kafka 메시지를 수신한다.
   - `EventEnvelope<JsonNode>` 파싱과 공통 필드 검증을 수행한다.
   - `NotificationEventHandlerRegistry`가 `eventType`에 맞는 handler를 선택한다.
   - handler가 `NotificationService`의 생성 메서드를 호출한다.
   - `NotificationService`가 중복 여부를 확인하고 `Notification`을 저장한다.
   - PUSH 채널이 허용된 타입이면 transaction commit 이후 `NotificationPushService`가 SSE 전송을 시도한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Kafka
       participant Consumer as NotificationEventConsumer
       participant Registry as HandlerRegistry
       participant Handler as EventHandler
       participant Service as NotificationService
       participant Repo as NotificationRepository
       participant Push as NotificationPushService
       participant SSE as SseEmitterRegistry

       Kafka->>Consumer: domain event message
       Consumer->>Consumer: envelope parse + validate
       Consumer->>Registry: get(eventType)
       Registry-->>Consumer: handler
       Consumer->>Handler: handle(envelope)
       Handler->>Service: create...Notification(...)
       Service->>Repo: existsByEventIdAndMemberIdAndType(...)
       Repo-->>Service: duplicate 여부
       Service->>Repo: save(Notification)
       Repo-->>Service: saved Notification
       Service-->>Push: afterCommit push(notification)
       Push->>SSE: find(memberId)
       SSE-->>Push: emitter or empty
       Push->>SSE: send notification event
   ```

2. 내 알림 목록 조회
   - `NotificationController`가 page, size와 현재 사용자 정보를 받는다.
   - `NotificationService`가 `memberId` 기준으로 최신순 페이지 조회를 수행한다.
   - `PagedResponse<NotificationResponse>`로 반환한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as NotificationController
       participant Service as NotificationService
       participant Repo as NotificationRepository

       Client->>Gateway: GET /api/notifications?page=&size=
       Gateway->>Controller: Forward request + current member
       Controller->>Service: getMyNotifications(memberId, page, size)
       Service->>Repo: findAllByMemberIdOrderByCreatedAtDesc(...)
       Repo-->>Service: Page<Notification>
       Service-->>Controller: PagedResponse<NotificationResponse>
       Controller-->>Gateway: 200 OK
       Gateway-->>Client: Response
   ```

3. SSE 구독
   - `NotificationSseController`가 현재 사용자 기준으로 `SseEmitter`를 생성한다.
   - `NotificationSseEmitterRegistry`에 memberId 단위로 emitter를 등록한다.
   - 기존 emitter가 있으면 교체하고, 연결 직후 `connected` 이벤트를 한번 보낸다.
   - 이후 알림 생성 후 `NotificationPushService`가 같은 memberId emitter로 `notification` 이벤트를 보낸다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as NotificationSseController
       participant Registry as NotificationSseEmitterRegistry
       participant Push as NotificationPushService

       Client->>Gateway: GET /api/notifications/stream
       Gateway->>Controller: Forward request + current member
       Controller->>Registry: register(memberId, emitter)
       Registry-->>Controller: emitter registered
       Controller-->>Client: connected event
       Push->>Registry: find(memberId)
       Registry-->>Push: emitter
       Push-->>Client: notification event
   ```

---

## 6. 이벤트 연동

### 6.1 발행 이벤트

Notification Service는 현재 도메인 이벤트를 발행하지 않는다.

---

### 6.2 소비 이벤트

| Topic | Event Type | 처리 목적 |
|---|---|---|
| `member-signed-up` | `MEMBER_SIGNED_UP` | 회원 가입 완료 알림 생성 |
| `member-seller-promoted` | `SELLER_PROMOTED` | 판매자 전환 알림 생성 |
| `member-account-verification-expired` | `ACCOUNT_VERIFICATION_EXPIRED` | 계좌 인증 만료 알림 생성 |
| `member-account-verification-failed` | `ACCOUNT_VERIFICATION_FAILED` | 계좌 인증 실패 알림 생성 |
| `member-oauth-linked` | `MEMBER_OAUTH_LINKED` | OAuth 연동 완료 알림 생성 |
| `order.created` | `ORDER_CREATED` | 구매자 주문 생성, 판매자 주문 접수 알림 생성 |
| `order.canceled` | `ORDER_CANCELED` | 구매자/판매자 주문 취소 알림 생성 |
| `payment.auto-purchase-confirmed` | `AUTO_PURCHASE_CONFIRMED` | 자동 구매 확정 알림 생성 |
| `payment.order-payment-result` | `ORDER_PAYMENT_SUCCEEDED`, `ORDER_PAYMENT_FAILED` | 결제 성공/실패 알림 생성 |
| `payment.seller-payout-result` | `SELLER_SETTLEMENT_PAYOUT_SUCCEEDED`, `SELLER_SETTLEMENT_PAYOUT_FAILED` | 정산 지급 결과 알림 생성 |
| `auction.bid.outbid` | `AUCTION_BID_OUTBID` | 상회 입찰 알림 생성 |
| `auction.won` | `AUCTION_WON` | 낙찰 알림 생성 |
| `auction.closed` | `AUCTION_CLOSED_SOLD`, `AUCTION_CLOSED_UNSOLD` | 경매 종료 알림 생성 |

특징:

- 하나의 `NotificationEventConsumer`가 여러 topic을 공통 listener로 처리한다.
- 실제 분기 기준은 topic이 아니라 `EventEnvelope.eventType`이다.
- payload 파싱은 각 handler가 담당하고, 공통 envelope 검증은 consumer가 수행한다.

---

### 6.3 실패 처리

현재 실패 처리 전략:

- envelope 파싱 실패, 지원하지 않는 eventType, 필수 필드 누락은 즉시 DLQ로 보낸다.
- 일시적 처리 오류는 Kafka listener error handler가 재시도 후 DLQ로 보낸다.
- DLQ payload는 `notification.dlq` topic으로 발행한다.
- 저장은 성공했지만 SSE 전송이 실패하면 `NotificationStatus.FAILED`로 바꾸고 emitter를 제거한다.
- 중복 이벤트는 예외 없이 무시하고 계측만 남긴다.

---

## 7. 외부 의존성

- PostgreSQL: 알림 inbox 영속 저장
- Kafka: 도메인 이벤트 소비, DLQ 발행
- Gateway / common-security: 인증된 사용자 문맥 복원
- SSE client connection: 브라우저 실시간 알림 전달
- common-monitoring: Actuator / Prometheus 메트릭 노출

---

## 8. 보안 / 인가

- 알림 조회와 읽음 처리는 모두 `@CurrentMember` 기준으로 현재 사용자에게만 허용된다.
- `markAsRead`는 `notification.memberId`와 현재 사용자 ID가 다르면 `NotificationNotFoundException`으로 거절한다.
- SSE 구독도 인증된 사용자만 가능하며, emitter는 memberId 기준으로 관리된다.
- Gateway가 1차 인증과 role 검사를 수행하고, Notification Service는 소유권 검증을 담당한다.

---

## 9. 트랜잭션 / 일관성

- 알림 저장과 읽음 처리는 JPA transaction 안에서 처리된다.
- 실시간 PUSH는 `afterCommit` 동기화로 실행되어, DB 저장이 commit된 뒤에만 시도된다.
- 이 구조 덕분에 "푸시는 됐지만 DB에는 없는 알림" 상태를 줄인다.
- 반대로 DB 저장은 성공했는데 SSE 전송이 실패하면 inbox에는 남고, 실시간 전달만 누락될 수 있다.
- 이벤트 재전송에 대비해 `existsByEventIdAndMemberIdAndType`와 DB 유니크 인덱스로 멱등성을 확보한다.

---

## 10. 운영 메모

- `NotificationType`별로 `INBOX`, `PUSH` 채널 지원 여부가 다르다. 모든 알림이 SSE로 전송되는 것은 아니다.
- 현재 SSE registry는 프로세스 메모리 기반이라 다중 인스턴스 환경에서는 같은 사용자의 연결이 특정 Pod에만 존재한다.
- `application.yml`의 로컬 기본 schema는 `notification`인데, 초기화 SQL은 `notification_service` schema를 만든다. 환경별 DB schema 설정을 함께 확인해야 한다.
- Kafka retry 설정은 `notification.kafka.retry.*` 프로퍼티로 조정한다.
- 알림 품질은 upstream 서비스의 event contract 안정성에 직접 의존한다.

---

## 11. 관련 파일

- [NotificationController.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/presentation/controller/NotificationController.java)
- [NotificationSseController.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/presentation/controller/NotificationSseController.java)
- [NotificationService.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/application/service/NotificationService.java)
- [NotificationPushService.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/application/service/NotificationPushService.java)
- [NotificationEventConsumer.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/infrastructure/messaging/kafka/consumer/NotificationEventConsumer.java)
- [NotificationKafkaConsumerConfig.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/infrastructure/config/NotificationKafkaConsumerConfig.java)
- [Notification.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/domain/entity/Notification.java)
- [NotificationSseEmitterRegistry.java](/C:/my_project/GoodsMall_BE/service/notification/src/main/java/com/example/notification/infrastructure/sse/NotificationSseEmitterRegistry.java)

---

## 12. 관련 문서

- [04-request-flow.md](/C:/my_project/GoodsMall_BE/docs/04-request-flow.md)
- [05-event-strategy.md](/C:/my_project/GoodsMall_BE/docs/05-event-strategy.md)
- [06-auth-flow.md](/C:/my_project/GoodsMall_BE/docs/06-auth-flow.md)
- [08-deployment.md](/C:/my_project/GoodsMall_BE/docs/08-deployment.md)
