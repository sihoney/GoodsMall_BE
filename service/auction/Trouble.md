# 경매 입찰 동시성 문제 - Trouble Report

## 현상

경매 종료 직전처럼 동시 입찰이 몰리는 상황에서, 한 입찰이 처리되는 동안 들어오는 다른 입찰들이 사실상 무시되거나 긴 대기 후 타임아웃된다.
예) 5000원 / 10000원 / 20000원 순서로 오게 동시에 온경우, 5000원만 PENDING 으로 만들고 나머지는 전부 거절해서 생성이 되지 않는 문제

---

## 원인: 비관적 락(Pessimistic Lock)의 과도한 잠금 범위

### 현재 흐름

```
입찰 요청 (BidCreateService)
  → findByIdWithLock()          ← SELECT FOR UPDATE (auction 행 잠금 시작)
  → validatePendingBid()
  → Bid.placePending() 저장
  → Kafka 이벤트 발행 (AUCTION_BID_FEE_CHARGE_REQUESTED)
  → 트랜잭션 커밋              ← 잠금 해제

Kafka Consumer (수수료 차감 완료/실패) - BidChargeCompletedConsumer, BidFeeChargeFailedConsumer
  → findByIdWithLock()          ← 다시 auction 행 잠금
  → 경매 최고가 업데이트 or 롤백
  → 트랜잭션 커밋              ← 잠금 해제
```

### 잠금이 유지되는 동안 일어나는 일

```
입찰 A → [auction 행 잠금] ─────────────────────────→ 커밋 → 해제
입찰 B                    → DB 레벨 대기 ─→ 대기 ─→ 대기 → 겨우 시작
입찰 C                    → DB 레벨 대기 ─→ ...타임아웃
```

- `SELECT FOR UPDATE`는 auction 행 전체를 잠그기 때문에, 잠금이 풀릴 때까지 다른 모든 입찰 트랜잭션이 DB 레벨에서 블로킹된다.
- 잠금 유지 시간이 길다: 검증 → Bid 저장 → Kafka 발행까지 한 트랜잭션 안에 있어서, 단순 INSERT보다 훨씬 오래 잠긴다.
- 수수료 차감(Kafka 왕복) 이후 두 번째 `findByIdWithLock()`도 동일한 문제를 가진다.

### 영향을 받는 코드 위치

| 파일 | 위치 | 잠금 사유 |
|------|------|---------|
| `BidCreateService` | `place()` | 입찰 접수 시 경매 검증 및 최고가 선점 |
| `BidFeeChargeCompletedConsumer` | `handle()` | 수수료 차감 완료 후 최고가 업데이트 |
| `BidFeeChargeFailedConsumer` | `handle()` | 수수료 차감 실패 후 최고가 롤백 |
| `AuctionSchedulerService` | `closeExpiredAuction()` | 경매 종료 시 낙찰/유찰 처리 |

---

## 개선 방향: 낙관적 락(Optimistic Lock) + 입찰 큐 패턴

### 핵심 아이디어

입찰을 일단 전부 받아놓고 (PENDING 저장, 잠금 없음), 처리 시점에 충돌을 감지한다.

### 변경 후 흐름

```
[1단계: 입찰 접수 (빠름, 잠금 없음)]
입찰 요청
  → 기본 유효성만 검사 (금액 형식, 경매 상태, 입찰 단위)
  → Bid를 PENDING으로 즉시 저장  ← auction 행 잠금 없음
  → 202 Accepted 즉시 반환

[2단계: 입찰 처리 (비동기)]
Kafka Consumer or 스케줄러
  → PENDING 입찰 조회 (금액 높은 순, 시간 순)
  → 예치금 검증
  → auction.updateHighestPrice() 시도
      → 성공: Bid ACTIVE, WebSocket 브로드캐스트
      → OptimisticLockException (다른 입찰이 먼저 처리됨): Bid FAILED 처리
```

### 필요한 코드 변경

1. **Auction 엔티티**: `@Version Long version` 필드 추가
2. **BidCreateService.place()**: `findByIdWithLock()` → `findById()` 변경 (잠금 제거)
3. **BidFeeChargeCompletedConsumer**: `OptimisticLockException` catch → 해당 Bid FAILED 처리
4. **스키마**: `auction` 테이블에 `version` 컬럼 추가 (`init_*.sql` 수정 필요)

---

## 세 가지 락 방식 비교

| | 비관락 (현재) | 낙관락 (개선안) | 분산락 (Redis) |
|---|---|---|---|
| **잠금 위치** | DB 행 (`SELECT FOR UPDATE`) | 없음 (`@Version` 감지) | 외부 저장소 |
| **다른 요청** | DB에서 블로킹 | 블로킹 없음 | Redis에서 블로킹 |
| **충돌 감지** | 쓰기 전 (선점) | 쓸 때 (사후 감지) | 쓰기 전 (선점) |
| **충돌 처리** | 자동 (대기 후 순차) | 직접 구현 (retry or reject) | 자동 (대기 후 순차) |
| **데드락 위험** | 있음 | 없음 | 없음 |
| **경매 적합성** | 낮음 (직렬화로 병목) | 높음 (병렬 접수, 선별 처리) | 중간 (다중 인스턴스 조율용) |

> 분산락과 낙관락의 차이: 분산락은 "한 번에 한 명만 실행"을 강제(선점)하고, 낙관락은 "다 실행하되 충돌 시 패자를 거절"하는 방식이다. 비관락과 분산락은 철학이 같고(선점), 낙관락만 후감지 방식이다.

---

## 개선 후 기대 효과

| 항목 | 현재 (비관락) | 개선 후 (낙관락) |
|------|-------------|----------------|
| 입찰 응답 속도 | Kafka 왕복 후 응답 | 즉시 202 Accepted |
| 동시 입찰 처리 | 직렬화 (한 번에 하나) | 병렬 접수, 선별 처리 |
| DB 락 경쟁 | auction 행 핫스팟 발생 | 락 경쟁 없음 |
| 데드락 위험 | 존재 | 없음 |
| 입찰 유실 | 타임아웃으로 유실 가능 | PENDING 상태로 보존 |
| 트레이드오프 | 구현 단순 | OptimisticLockException 처리 로직 필요 |

---

## 성능 개선의 실체: 트랜잭션 분리

### 진짜 병목 구간

Lock이 잡히는 구간을 정확히 보면 문제가 명확해진다.

```
BidCreateService.place() 트랜잭션
┌─────────────────────────────────────────┐
│ findByIdWithLock()  ← Lock 획득          │
│ validatePendingBid()                    │  ← 이 모든 것이 Lock 안에 있음
│ Bid INSERT (placePending)               │
│ Outbox Event INSERT                     │
│ 트랜잭션 커밋       ← Lock 해제            │
└─────────────────────────────────────────┘
```

입찰 접수는 각자의 Bid를 INSERT하는 것이라 서로 충돌할 이유가 없다. 그런데 auction을 Lock으로 잡고 그 안에서 처리하니 직렬화가 발생한다.

### 성능 개선의 본체

**낙관락 자체가 아니라 "입찰 접수 트랜잭션에서 auction Lock 제거"가 핵심이다.**

```
[개선 후]
입찰 A: Bid INSERT (Lock 없음) → 즉시 완료
입찰 B: Bid INSERT (Lock 없음) → 즉시 완료
입찰 C: Bid INSERT (Lock 없음) → 즉시 완료

↓ 나중에 Consumer에서

auction 최고가 UPDATE 시에만 @Version으로 충돌 감지
```

낙관락은 업데이트 시점의 **정확성 안전망**이고, 성능 개선은 Lock 없이 Bid만 INSERT하는 **트랜잭션 분리**에서 나온다.

---

## Kafka가 Thundering Herd를 흡수하는 이유

### Thundering Herd란?

낙관락의 일반적 약점: 100명이 동시 입찰하면 1명만 성공하고 99명이 즉시 재시도 → 또 충돌 → 재시도 폭주 → DB 부하 급증.

### 이 시스템에서 발생하지 않는 이유

```
100개 입찰 요청
    ↓ 전부 즉시 PENDING 저장 (Lock 없음, 각자 Bid INSERT)
    ↓ Kafka 메시지 100개 적재
    ↓
Kafka Consumer → 파티션 단위로 순차 소비
    ↓
실제 DB 동시 접근 = Consumer 인스턴스 수 (보통 2~4개)
```

100명이 동시에 재시도하는 상황 자체가 발생하지 않는다. Kafka가 **자연스러운 동시성 제한자(throttle)** 역할을 해서 충돌 가능한 동시 처리 수를 Consumer 수로 제한한다.

---

## 분산락(Redis)이 이 시스템에서 안티패턴인 이유

```
[낙관락]
100명 입찰 → 전부 즉시 202 Accepted → Kafka 큐에서 순차 처리

[분산락]
100명 입찰 → Redis 락 경쟁 → 한 명씩 처리 → 마감 직전 99번째는 98번 대기
```

이미 Kafka가 직렬화를 담당하는데, 앞단에 또 다른 직렬화 메커니즘(분산락)을 끼워넣으면 **"즉시 접수 → 비동기 처리"라는 큐의 이점을 무력화**하는 중복 안티패턴이 된다.

분산락이 실제로 유용한 경우는 **동기 직접 처리 구조(Kafka 큐 없음)** 또는 **스케줄러 중복 실행 방지**다.

| 구조 | 적합한 선택 |
|---|---|
| 동기 직접 처리 (큐 없음) | 분산락 |
| Kafka 큐 + 비동기 처리 (현재 시스템) | 낙관락 |
| 스케줄러 중복 실행 방지 | 분산락 (Redis ShedLock) |

---

## 추가 최적화 포인트

### 1. Kafka 파티션 키를 auctionId로 설정

같은 경매의 입찰이 단일 Consumer에서 순차 처리되도록 하면, OptimisticLockException 발생 자체를 거의 0에 수렴시킬 수 있다. 낙관락이 사실상 "안전망" 역할만 하게 된다.

```
파티션 키 = auctionId
→ 같은 경매 입찰은 동일 파티션 → 단일 Consumer 순차 처리
→ 동시 업데이트 시도가 없음 → OptimisticLockException 거의 발생 안 함
```

### 2. Saga 보상 트랜잭션

수수료 차감 실패 시 롤백 흐름은 락 방식과 무관하게 반드시 설계에 포함되어야 한다. 현재 `BidFeeChargeFailedConsumer`가 이 역할을 담당하고 있다.

---

## 낙관락 상세 동작 Q&A

### Q. 분산락(Redis)이 더 빠른 거 아닌가?

Redis는 인메모리라 DB보다 빠른 건 맞다. 하지만 분산락의 철학은 비관락과 동일하다 — "한 번에 하나만 실행". 잠금 장소가 DB에서 Redis로 옮겨졌을 뿐, 동시 입찰이 블로킹된다는 문제는 그대로다.

경매에서 진짜 문제는 "잠금이 느려서"가 아니라 "왜 잠금을 걸고 있는가"다.

```
[비관락 - DB]    입찰들 → DB 레벨 블로킹 → 순차 처리
[분산락 - Redis] 입찰들 → Redis 레벨 블로킹 → 순차 처리  ← 빠르지만 여전히 블로킹
[낙관락]         입찰들 → 전부 즉시 접수 → 처리 시 충돌 감지  ← 블로킹 없음
```

Redis 분산락이 유용한 곳은 따로 있다: **스케줄러 중복 실행 방지** (여러 인스턴스 중 하나만 실행되도록).

```
입찰 접수/처리 동시성   → 낙관락 (@Version)
스케줄러 중복 실행 방지 → 분산락 (Redis ShedLock)
```

### Q. 스케줄러가 처리하면 느리지 않나?

스케줄러가 메인 경로면 느리다. 하지만 현재 코드는 이미 이벤트 드리븐이다.

트랜잭션 커밋 직후 `OutboxEventPendingTrigger`가 발행되면 `OutboxRelayService`가 즉시 Kafka에 발행한다. 스케줄러 대기가 없다.

```
POST /bids → PENDING 저장 → 커밋
                              ↓ @TransactionalEventListener(AFTER_COMMIT)
                         즉시 Kafka 발행
                              ↓
                    BidFeeChargeCompletedConsumer
                              ↓
                         broadcast
```

낙관락으로 바꿔도 이 흐름은 그대로다. Kafka 왕복 속도도 동일하다. 스케줄러(`@Scheduled fixedDelay=5000`)는 누락된 이벤트 재시도용 안전망일 뿐이다.

### Q. 5000원으로 업데이트 후 4000원이 들어오면 비교해서 높으면 바꾸는 건가?

단순 비교만으로는 안 된다. 동시에 읽으면 둘 다 "내가 유효하다"고 판단하는 버그가 생긴다.

```
초기: current_highest = 3000, version = 5

Consumer A (5000원): 읽음 → 3000, 5000 > 3000 ✓
Consumer B (4000원): 읽음 → 3000, 4000 > 3000 ✓  ← 동시에 읽음

Consumer A: UPDATE → 성공 (version 5→6, highest=5000)
Consumer B: UPDATE → 성공 (version 6→7, highest=4000)  ← 버그: 4000이 최고가
```

`@Version`이 이걸 막는다.

```
Consumer B: UPDATE WHERE version=5 → 이미 6이라 0행 업데이트
                                      → OptimisticLockException
```

### Q. version이 맞지 않으면 새 버전을 읽어서 비교하는 건가?

맞다. 예외 발생 시 최신 상태를 다시 읽고 재비교 후 재시도한다.

```
OptimisticLockException
    ↓
다시 읽음 (최신 version=6, highest=5000)
    ↓
내 입찰(4000) > current(5000)? → NO → Bid FAILED (재시도 중단)
내 입찰(5000) > current(3500)? → YES → UPDATE WHERE version=6 재시도
```

### Q. 그러면 계속 재시도되는 거 아닌가?

경매의 특성상 자연스럽게 수렴한다.

예외가 발생했다는 건 나보다 먼저 누군가 업데이트했다는 뜻이다. 재시도 시 최신 최고가를 보게 되는데, 내 입찰가보다 높으면 즉시 FAILED로 끝난다.

```
5000원이 먼저 성공 → version 5→6, highest=5000

3000원 재시도: highest=5000, 3000 < 5000 → 즉시 FAILED
4000원 재시도: highest=5000, 4000 < 5000 → 즉시 FAILED
```

낮은 입찰들은 재시도 1번에 끝난다. 다만 같은 금액 동시 입찰 등 극단적 상황을 위해 최대 재시도 횟수를 안전망으로 둔다.

```java
int maxRetry = 3;
int attempt = 0;

while (attempt < maxRetry) {
    try {
        // 읽고 비교하고 업데이트
        break;
    } catch (OptimisticLockException e) {
        attempt++;
        if (attempt == maxRetry) {
            bid.fail(); // 재시도 초과 → FAILED
        }
    }
}
```
