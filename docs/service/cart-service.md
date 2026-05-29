# Cart Service

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

Cart Service는 주문 전 단계의 사용자 임시 구매 상태를 관리하는 서비스다. 현재 구현 범위는 장바구니와 찜 목록이다.

핵심 책임:

- 장바구니 조회
- 장바구니 상품 추가, 수량 변경, 삭제, 전체 비우기
- 주문 완료 후 장바구니 항목 정리
- 찜 목록 조회
- 찜 토글
- 찜 항목을 장바구니로 이동
- 장바구니 추가 이벤트 발행

이 서비스는 상품 원본 정보나 주문 확정 상태를 소유하지 않는다. `memberId`, `productId`, `quantity` 같은 최소 상태만 소유한다.

---

## 2. 소유 도메인 / 데이터

주요 영속 데이터:

- `Cart`
- `Wish`

주요 상태 규칙:

- `Cart`는 `(memberId, productId)` 조합의 임시 구매 상태를 표현한다.
- 수량은 최소 1 이상이어야 한다.
- 회원당 장바구니 항목 수는 최대 10개로 제한한다.
- `Wish`는 `(member_id, product_id)` 유니크 제약으로 중복 찜을 막는다.

현재 구조상 `CartItem` 같은 별도 엔티티는 없고, `Cart` 엔티티 자체가 장바구니 한 줄(item) 역할을 한다.

---

## 3. 주요 유스케이스

- 내 장바구니 조회
- 장바구니 상품 추가
- 장바구니 수량 변경
- 장바구니 일부 상품 삭제
- 장바구니 전체 비우기
- 주문 완료 상품 정리
- 내 찜 목록 조회
- 찜 추가/해제 토글
- 찜 상품을 장바구니로 이동

---

## 4. API 표면

주요 외부 API:

| Endpoint | Method | Purpose | Auth |
|---|---|---|---|
| `/api/carts` | `GET` | 내 장바구니 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/carts` | `DELETE` | 장바구니 전체 비우기 | `USER`, `SELLER`, `ADMIN` |
| `/api/carts/items` | `POST` | 장바구니 상품 추가 | `USER`, `SELLER`, `ADMIN` |
| `/api/carts/items/{cartId}` | `PATCH` | 장바구니 수량 변경 | `USER`, `SELLER`, `ADMIN` |
| `/api/carts/items` | `DELETE` | 장바구니 일부 상품 삭제 | `USER`, `SELLER`, `ADMIN` |
| `/api/carts/complete-ordered` | `DELETE` | 주문 완료 상품 정리 | internal / current direct call |
| `/api/wishes` | `GET` | 내 찜 목록 조회 | `USER`, `SELLER`, `ADMIN` |
| `/api/wishes/{productId}` | `POST` | 찜 토글 | `USER`, `SELLER`, `ADMIN` |
| `/api/wishes/{wishId}/to-cart` | `POST` | 찜 상품 장바구니 이동 | `USER`, `SELLER`, `ADMIN` |

특징:

- 모든 사용자 API는 `@CurrentMember`를 사용한다.
- `complete-ordered`는 현재 외부 이벤트 consumer가 아니라 직접 호출용 endpoint로 남아 있다.
- 장바구니 API는 product 상세를 조회하지 않고 product ID만 저장한다.

---

## 5. 서비스 내부 요청 흐름

대표 흐름:

1. 장바구니 상품 추가
   - `CartController`가 현재 사용자와 `productId`, `quantity`를 받는다.
   - `CartUpdateService`가 중복 상품 여부와 최대 개수 제한을 검사한다.
   - `Cart`를 저장한 뒤 `cart.item.added` 이벤트를 발행한다.
   - 최신 장바구니 상태를 다시 조회해 반환한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as CartController
       participant Service as CartUpdateService
       participant Repo as CartRepository
       participant Kafka as CartEventPublisher

       Client->>Gateway: POST /api/carts/items
       Gateway->>Controller: Forward request + current member
       Controller->>Service: addCartItem(memberId, request)
       Service->>Repo: existsByMemberIdAndProductId(...)
       Service->>Repo: countCartItems(memberId)
       Service->>Repo: save(Cart)
       Service->>Kafka: publishCartItemAdded(memberId, productId)
       Service->>Repo: findAllByMemberId(memberId)
       Repo-->>Service: updated cart items
       Service-->>Controller: CartResponse
       Controller-->>Gateway: 201 Created
       Gateway-->>Client: Response
   ```

2. 찜 토글
   - `WishController`가 현재 사용자와 상품 ID를 받는다.
   - `WishCreateService`가 이미 찜이 있으면 삭제하고, 없으면 새로 저장한다.
   - 결과는 `WishToggleResponse`로 반환한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as WishController
       participant Service as WishCreateService
       participant Repo as WishRepository

       Client->>Gateway: POST /api/wishes/{productId}
       Gateway->>Controller: Forward request + current member
       Controller->>Service: toggleWish(memberId, productId)
       Service->>Repo: existsByMemberIdAndProductId(...)
       alt already exists
           Service->>Repo: deleteByMemberIdAndProductId(...)
           Service-->>Controller: WishToggleResponse(false)
       else not exists
           Service->>Repo: save(Wish)
           Service-->>Controller: WishToggleResponse(true)
       end
       Controller-->>Gateway: 200 OK
       Gateway-->>Client: Response
   ```

3. 찜 상품 장바구니 이동
   - `WishDeleteService`가 찜 소유권을 확인한다.
   - 내부적으로 `CartUpdateUseCase.addCartItem`을 호출해 장바구니에 추가한다.
   - 장바구니 추가가 성공하면 기존 `Wish`를 삭제한다.

   ```mermaid
   sequenceDiagram
       autonumber
       participant Client
       participant Gateway
       participant Controller as WishController
       participant WishService as WishDeleteService
       participant WishRepo as WishRepository
       participant CartService as CartUpdateUseCase
       participant CartRepo as CartRepository

       Client->>Gateway: POST /api/wishes/{wishId}/to-cart
       Gateway->>Controller: Forward request + current member
       Controller->>WishService: moveToCart(memberId, wishId)
       WishService->>WishRepo: findById(wishId)
       WishRepo-->>WishService: Wish
       WishService->>CartService: addCartItem(memberId, productId, quantity=1)
       CartService->>CartRepo: save(Cart)
       WishService->>WishRepo: delete(Wish)
       Controller-->>Gateway: 204 No Content
       Gateway-->>Client: Response
   ```

---

## 6. 이벤트 연동

### 6.1 발행 이벤트

| Topic | Payload | When | Delivery |
|---|---|---|---|
| `cart.item.added` | `CartItemAddedEvent` | 장바구니 상품 추가 후 | direct publish |

특징:

- 현재 발행 이벤트는 공통 `EventEnvelope`를 사용하지 않는다.
- 발행 실패는 예외를 다시 던지지 않고 warn 로그만 남긴다.

---

### 6.2 소비 이벤트

Cart Service는 현재 Kafka 도메인 이벤트 consumer를 구현하고 있지 않다.

주의:

- 문서상 기대와 달리 `order.confirmed` consumer는 현재 코드에 없다.
- 주문 완료 후 장바구니 정리는 `DELETE /api/carts/complete-ordered` endpoint로 직접 호출하는 상태다.

---

### 6.3 실패 처리

현재 실패 처리 전략:

- 중복 상품 추가는 `CartItemDuplicateException`으로 거절한다.
- 장바구니 최대 개수 초과는 `CartLimitExceededException`으로 거절한다.
- 이벤트 발행 실패는 장바구니 저장을 롤백하지 않고 로그만 남긴다.
- `moveToCart`는 내부적으로 장바구니 추가를 먼저 수행하므로, 추가 실패 시 찜 삭제는 일어나지 않는다.

---

## 7. 외부 의존성

- PostgreSQL: 장바구니와 찜 상태 저장
- Kafka: 장바구니 추가 이벤트 발행
- common-security: `@CurrentMember` 기반 사용자 문맥 복원
- common-monitoring: Actuator / Prometheus 메트릭 노출

현재 구현에는 Product Service를 직접 호출하는 client는 없다. product 유효성은 장바구니 저장 시점에 확인하지 않는다.

---

## 8. 보안 / 인가

- 모든 사용자 API는 Gateway role rule 뒤에서 동작한다.
- `Cart.validateOwner`, `Wish.validateWishOwner`로 소유권을 서비스 내부에서 다시 검증한다.
- `complete-ordered` endpoint는 현재 별도 인증 문맥 없이 memberId를 body로 받으므로, 외부 공개보다는 내부 호출 전제로 보는 편이 맞다.

---

## 9. 트랜잭션 / 일관성

- cart/wish 변경은 단일 DB transaction으로 처리된다.
- 장바구니 추가 후 Kafka 발행은 같은 메서드 안에서 직접 호출되지만, 발행 실패를 예외로 올리지 않기 때문에 DB 상태와 이벤트 상태가 어긋날 수 있다.
- 주문 완료 장바구니 정리는 현재 비동기 이벤트가 아니라 동기 endpoint 호출에 의존한다.
- 상품 삭제나 가격 변경과 장바구니 상태 간 정합성은 별도로 자동 보정되지 않는다.

---

## 10. 운영 메모

- `cart.item.added` topic 이름은 event strategy 문서의 envelope 기반 명명과 다르다.
- 장바구니는 상품 스냅샷을 저장하지 않으므로, 화면 구성 시 product 정보를 별도 조회해야 한다.
- 현재 문서 기준으로는 주문 완료 후 cart 정리가 eventual consistency 이벤트 흐름으로 구현되어 있지 않다.
- 로컬 DB 기본 사용자/비밀번호가 다른 서비스와 다르게 `goods/goods`로 잡혀 있다.

---

## 11. 관련 파일

- [CartController.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/cart/presentation/controller/CartController.java)
- [WishController.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/wish/presentation/controller/WishController.java)
- [CartUpdateService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/cart/application/service/CartUpdateService.java)
- [CartDeleteService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/cart/application/service/CartDeleteService.java)
- [CartSearchService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/cart/application/service/CartSearchService.java)
- [WishCreateService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/wish/application/service/WishCreateService.java)
- [WishDeleteService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/wish/application/service/WishDeleteService.java)
- [WishSearchService.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/wish/application/service/WishSearchService.java)
- [CartEventPublisher.java](/C:/my_project/GoodsMall_BE/service/cart/src/main/java/com/example/cartservice/cart/infrastructure/kafka/CartEventPublisher.java)

---

## 12. 관련 문서

- [04-request-flow.md](/C:/my_project/GoodsMall_BE/docs/04-request-flow.md)
- [05-event-strategy.md](/C:/my_project/GoodsMall_BE/docs/05-event-strategy.md)
- [06-auth-flow.md](/C:/my_project/GoodsMall_BE/docs/06-auth-flow.md)
- [08-deployment.md](/C:/my_project/GoodsMall_BE/docs/08-deployment.md)
