# Product Service

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

Product Service는 상품 카탈로그와 카테고리, 이미지, 재고, 검색 인덱싱 연계를 소유하는 서비스다.

핵심 책임:

- 상품 등록, 조회, 수정, 삭제, 복구
- 상품 재고 증감과 판매 상태 관리
- 카테고리 생성, 조회, 수정, 삭제
- 상품 이미지 업로드, 썸네일 변경, 삭제
- 주문 가능 여부 확인과 주문 시점 재고 차감
- 상품 변경 이벤트 outbox 저장과 Kafka relay
- Elasticsearch 검색 인덱스 동기화

Order, Auction, Cart 같은 다른 서비스는 상품 ID를 참조할 수 있지만, 상품 원본 정보와 재고 원본 상태는 Product Service가 소유한다.

---

## 2. 소유 도메인 / 데이터

주요 영속 데이터:

- `Product`
- `Category`
- `ProductImage`
- `OutboxEvent`

주요 상태:

- `Product.status`: `ACTIVE`, `INACTIVE`, `SOLD_OUT`
- `Product.deletedAt`: 소프트 삭제 여부
- `Product.stockQuantity`: 주문 가능 수량
- `OutboxEvent.status`: `PENDING`, `PROCESSING`, `PUBLISHED`

주요 규칙:

- 재고가 0이 되면 `ACTIVE -> SOLD_OUT`으로 바뀐다.
- 재고가 다시 생기면 `SOLD_OUT -> ACTIVE`로 복귀할 수 있다.
- 상품 삭제는 하드 삭제가 아니라 `deletedAt`과 상태 변경을 이용한 소프트 삭제다.
- 카테고리는 최대 depth 2까지 생성할 수 있다.

---

## 3. 주요 유스케이스

- 판매자 상품 등록과 이미지 업로드
- 판매자 상품 수정, 재고 조정, 판매 상태 변경, 복구
- 상품 목록/상세/인기 상품 조회
- 장바구니 화면용 상품 ID 목록 조회
- 주문 생성 전 상품별 주문 가능 여부 확인과 재고 차감
- 경매 낙찰, 주문 취소, 결제 실패 이벤트에 따른 재고 보정
- 관리자/운영자용 ES 재인덱싱

---

## 4. API 표면

주요 외부 API:

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/products` | `POST` | 상품 등록 | `SELLER`, `ADMIN` |
| `/api/products/check-availability` | `POST` | 주문 가능 여부 확인 + 재고 차감 | authenticated internal flow |
| `/api/products/by-ids` | `GET` | 상품 ID 목록 조회 | public |
| `/api/products` | `GET` | 상품 검색/목록 조회 | public |
| `/api/products/popular` | `GET` | 인기 상품 조회 | public |
| `/api/products/admin/all` | `GET` | 전체 상품 조회 | `ADMIN` |
| `/api/products/seller` | `GET` | 현재 판매자 상품 조회 | `SELLER`, `ADMIN` |
| `/api/products/{productId}` | `GET` | 상품 상세 조회 | public |
| `/api/products/{productId}` | `PUT` | 상품 수정 | `SELLER`, `ADMIN` |
| `/api/products/{productId}` | `DELETE` | 상품 삭제 | `SELLER`, `ADMIN` |
| `/api/products/{productId}/stock/increase` | `PATCH` | 재고 증가 | `SELLER`, `ADMIN` |
| `/api/products/{productId}/stock/decrease` | `PATCH` | 재고 감소 | `SELLER`, `ADMIN` |
| `/api/products/{productId}/status` | `PATCH` | 상품 상태 변경 | `SELLER`, `ADMIN` |
| `/api/products/{productId}/restore` | `POST` | 삭제 상품 복구 | `SELLER`, `ADMIN` |
| `/api/products/{productId}/images` | `POST` | 이미지 업로드 | authenticated seller flow |
| `/api/products/{productId}/images/{imageId}/thumbnail` | `PATCH` | 썸네일 변경 | authenticated seller flow |
| `/api/products/{productId}/images/{imageId}` | `DELETE` | 이미지 삭제 | authenticated seller flow |
| `/api/categories/admin` | `POST` | 관리자 카테고리 생성 | `ADMIN` |
| `/api/categories` | `POST` | 판매자 카테고리 생성 | `SELLER`, `ADMIN` |
| `/api/categories/**` | `GET`, `PUT`, `DELETE` | 카테고리 조회/수정/삭제 | mixed |
| `/internal/products/sellers/{sellerId}/withdrawal-summary` | `GET` | 판매자 탈퇴 가능 여부 보조 조회 | internal |
| `/api/admin/products/reindex` | `POST` | ES 전체 재인덱싱 | `ADMIN` |

특징:

- 조회 API는 public 비중이 높고, 변경 API는 Gateway role rule 뒤에서 동작한다.
- `check-availability`는 단순 조회가 아니라 주문 가능 여부 판정과 재고 차감을 같이 수행한다.
- 상품 등록 시 multipart + JSON 조합을 사용한다.

---

## 5. 서비스 내부 요청 흐름

대표 흐름:

1. 상품 등록
   - `ProductController`가 multipart 요청을 받는다.
   - `ProductCreateService`가 카테고리와 입력값을 검증하고 `Product`를 저장한다.
   - 이미지를 S3에 업로드하고 `ProductImage`를 저장한다.
   - Elasticsearch에 직접 인덱싱을 시도한다.
   - `ProductOutboxEventService`가 `OutboxEvent`를 저장하고 relay trigger를 발행한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as ProductController
       participant Service as ProductCreateService
       participant DB as ProductRepository
       participant S3 as FileStorageRepository
       participant ES as ProductSearchRepository
       participant Outbox as ProductOutboxEventService

       Client->>Gateway: POST /api/products
       Gateway->>Controller: Forward request + current member
       Controller->>Service: createProduct(...)
       Service->>DB: save(Product)
       DB-->>Service: saved Product
       Service->>S3: upload images
       S3-->>Service: s3 keys
       Service->>DB: save(ProductImage...)
       Service->>ES: index(product)
       Service->>Outbox: saveCreatedEvent(product)
       Outbox->>DB: save(OutboxEvent)
       Service-->>Controller: ProductResponse
       Controller-->>Gateway: 201 Created
       Gateway-->>Client: Response
   ```

2. 주문 가능 여부 확인과 재고 차감
   - `ProductController`가 상품 ID/수량 목록을 받는다.
   - `ProductUpdateService`가 각 상품을 lock 조회한다.
   - 판매 상태와 재고를 평가해 `ProductAvailabilityResponse`를 만든다.
   - 주문 가능 상품만 재고를 감소시킨다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Caller
       participant Controller as ProductController
       participant Service as ProductUpdateService
       participant DB as ProductRepository

       Caller->>Controller: POST /api/products/check-availability
       Controller->>Service: deductStock(productRequests)
       loop each product request
           Service->>DB: findByIdWithLock(productId)
           DB-->>Service: Product
           Service->>Service: orderable 여부 계산
           alt orderable
               Service->>DB: save(product with decreased stock)
           end
       end
       Service-->>Controller: List<ProductAvailabilityResponse>
       Controller-->>Caller: 200 OK
   ```

3. outbox relay
   - 상품 변경 중 저장된 `OutboxEvent`는 commit 이후 trigger 또는 주기 스케줄에 의해 처리된다.
   - `OutboxProcessor`가 `PENDING` 이벤트를 `PROCESSING`으로 claim한다.
   - Kafka 발행 성공 시 `PUBLISHED`로 바꾸고, 실패 시 다시 `PENDING`으로 되돌린다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Relay as OutboxRelayService
       participant Processor as OutboxProcessor
       participant Repo as OutboxEventRepository
       participant Kafka

       Relay->>Processor: processOutbox()
       Processor->>Repo: findAllByStatus(PENDING)
       loop each outbox event
           Processor->>Repo: changeToProcessingIfPending(id)
           Processor->>Kafka: send(topic, partitionKey, payload)
           alt publish success
               Processor->>Repo: changeToPublishedIfProcessing(id)
           else publish failure
               Processor->>Repo: changeToPendingIfProcessing(id)
           end
       end
   ```

---

## 6. 이벤트 연동

### 6.1 발행 이벤트

| Topic | Event Type | When | Delivery |
|---|---|---|---|
| `product.created` | `PRODUCT_CREATED` | 상품 등록 후 | outbox relay |
| `product.updated` | `PRODUCT_UPDATED` | 상품 수정, 상태 변경, 복구, 이미지 변경 후 | outbox relay |
| `product.deleted` | `PRODUCT_DELETED` | 상품 삭제 후 | outbox relay |
| `product.thumbnail-changed` | `PRODUCT_THUMBNAIL_CHANGED` | 썸네일 변경 후 | outbox relay |

특징:

- 현재 발행은 공통 `EventEnvelope`가 아니라 서비스 전용 message class를 직렬화해 topic에 보낸다.
- 트랜잭션 안에서는 즉시 publish하지 않고 `OutboxEvent`를 저장한다.
- commit 이후 relay trigger와 주기 스케줄이 둘 다 outbox 처리를 시도한다.

---

### 6.2 소비 이벤트

| Topic | 처리 목적 |
|---|---|
| `auction.won` | 낙찰 상품 재고 1 감소, 최종 가격 반영 |
| `order.canceled` | 주문 취소 라인별 재고 복원 |
| `order.payment-failed` | 결제 실패 라인별 재고 복원 |
| `product.created` | ES 인덱싱 |
| `product.updated` | ES 재인덱싱 |
| `product.deleted` | ES 인덱스 삭제 |

특징:

- `auction.won`, `order.canceled`, `order.payment-failed` 소비는 모두 재고 보정 목적이다.
- 상품 변경 이벤트는 같은 서비스 내부의 `ProductEsSyncConsumer`가 다시 소비해 ES를 맞춘다.
- ES는 상품 생성/삭제 시 직접 반영도 시도하고, Kafka 소비는 보조 동기화 경로로도 동작한다.

---

### 6.3 실패 처리

현재 실패 처리 전략:

- 상품 등록 중 S3 업로드가 실패하면 이미 업로드된 파일을 정리하고 예외를 던진다.
- ES 직접 인덱싱 실패는 로그만 남기고 등록/삭제 자체는 성공시킨다.
- outbox Kafka 발행 실패 시 이벤트를 다시 `PENDING`으로 돌려 재처리 대상으로 남긴다.
- 재고 복원 consumer는 예외를 로그로 남기지만 DLQ 같은 별도 실패 파이프라인은 보이지 않는다.

---

## 7. 외부 의존성

- PostgreSQL: 상품, 카테고리, 이미지, outbox 저장
- AWS S3: 상품 이미지 저장
- Elasticsearch: 상품 검색 인덱스
- Kafka: 상품 변경 이벤트 발행, 경매/주문/결제 이벤트 소비
- common-security: `@CurrentMember` 기반 사용자 문맥 복원
- common-monitoring: Actuator / Prometheus 메트릭 노출

---

## 8. 보안 / 인가

- 상품/카테고리 조회 일부는 public으로 열려 있다.
- 판매자 변경 API는 Gateway가 role을 걸러주고, Product Service가 `validateSeller`로 소유권을 다시 검증한다.
- 내부 withdrawal summary API는 외부 사용자 API가 아니라 서비스 간 호출용이다.
- 카테고리 API는 일부가 `@CurrentMember` 대신 헤더 직접 사용(`X-User-Id`)에 의존하므로 인증 헤더 규약 일관성을 주의해야 한다.

---

## 9. 트랜잭션 / 일관성

- 상품 생성/수정/삭제와 outbox 저장은 같은 트랜잭션 경계 안에서 처리된다.
- Kafka 발행은 outbox relay로 분리되어 DB commit 이후 일어난다.
- ES는 직접 반영과 Kafka 소비 반영이 혼합되어 있어서, 단기적으로는 중복 작업이지만 장애 시 보조 경로 역할을 한다.
- `check-availability`는 lock 조회 후 재고를 즉시 차감하므로, 호출자는 이 API를 idempotent하게 다뤄야 한다.

---

## 10. 운영 메모

- `findById`는 조회 시 `viewCount`를 증가시키므로 완전한 read-only 조회가 아니다.
- 로컬 Kafka bootstrap 기본값은 `localhost:29092`로 다른 서비스와 다르다.
- ES 인덱싱은 직접 호출과 Kafka consumer 두 경로가 있어서, 동기화 문제를 볼 때 둘 다 확인해야 한다.
- outbox recovery는 `OutboxRecoveryJob`과 `OutboxRelayService` 스케줄을 통해 보강된다.
- `order.payment-failed` topic 이름은 event strategy 문서의 envelope 기반 명명과 완전히 일치하지 않을 수 있다.

---

## 11. 관련 파일

- [ProductController.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/presentation/controller/ProductController.java)
- [CategoryController.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/presentation/controller/CategoryController.java)
- [ProductImageController.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/presentation/controller/ProductImageController.java)
- [ProductInternalController.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/presentation/controller/ProductInternalController.java)
- [ProductCreateService.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/application/service/ProductCreateService.java)
- [ProductUpdateService.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/application/service/ProductUpdateService.java)
- [ProductSearchService.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/application/service/ProductSearchService.java)
- [ProductOutboxEventService.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/infrastructure/messaging/kafka/ProductOutboxEventService.java)
- [OutboxProcessor.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/infrastructure/messaging/kafka/OutboxProcessor.java)
- [ProductEsSyncConsumer.java](/C:/my_project/GoodsMall_BE/service/product/src/main/java/com/example/product/infrastructure/messaging/kafka/ProductEsSyncConsumer.java)

---

## 12. 관련 문서

- [04-request-flow.md](/C:/my_project/GoodsMall_BE/docs/04-request-flow.md)
- [05-event-strategy.md](/C:/my_project/GoodsMall_BE/docs/05-event-strategy.md)
- [06-auth-flow.md](/C:/my_project/GoodsMall_BE/docs/06-auth-flow.md)
- [08-deployment.md](/C:/my_project/GoodsMall_BE/docs/08-deployment.md)
