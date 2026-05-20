# 경매 서비스 부하테스트 가이드

## 이 테스트가 증명하려는 것

경매 시스템은 일반 CRUD 서비스와 다른 핵심 문제를 가진다.

**동시에 여러 사람이 같은 경매에 입찰할 때 무결성이 보장되는가?**

- 중복 낙찰이 발생하지 않는가 (최종 ACTIVE 입찰은 경매당 1건)
- 낙찰가가 항상 최고가인가 (낮은 가격이 이기는 역전 현상 없음)
- 서버 오류(5xx) 없이 직렬화 처리되는가

이 테스트 스위트는 위 질문에 대한 수치적 근거를 제공한다.

---

## 핵심 지표 해석

### 공통 지표 (helpers/metrics.js)

모든 시나리오에서 동일한 이름으로 측정되는 입찰 결과 지표.

| 지표 | 의미 | 정상 판정 |
|------|------|-----------|
| `bid_success` | 입찰 성공 (201) | > 0 |
| `bid_rejected` | 비즈니스 규칙 거부 (400/409/422) | 높아도 정상 — 동시성 보호 로직이 작동한다는 의미 |
| `bid_server_error` | 서버 오류 (5xx) | 0에 가까울수록 좋음. 락 데드락/타임아웃 없음을 의미 |

> `bid_rejected`가 많다고 실패가 아니다.
> 동시 입찰 시 409(BidAlreadyPending)가 다수 나오는 것은 PENDING 단일 보장 로직이 정상 작동한다는 신호다.

### 응답시간 지표

| p99 범위 | 해석 |
|----------|------|
| < 500ms | 정상 — HikariCP 대기 없음 |
| 500ms ~ 2s | 경고 — HikariCP 대기 또는 락 경합 시작 |
| 2s ~ 5s | 위험 — DB 커넥션 포화 또는 락 타임아웃 임박 |
| > 5s | 한계 초과 |

### 5xx 에러 원인 분류 (stress 테스트 전용)

| 에러 코드 | 예상 원인 | 조치 |
|-----------|-----------|------|
| 500 | 코드 예외 (NPE, 상태 불일치) | 부하와 무관한 버그 — 즉시 수정 |
| 503 | HikariCP 커넥션 타임아웃 | pool size 증설 검토 |
| 504 | 락 대기 / 게이트웨이 타임아웃 | 트랜잭션 범위 축소 검토 |

---

## 각 테스트가 하는 일

### Smoke Test — 기능이 동작하는가?

부하를 가하기 전 기본 기능이 정상인지 확인하는 사전 검증이다.
VU 2명이 목록 조회 → 상세 조회 → 입찰 목록 조회 → 입찰 순서로 실제 사용자 흐름을 재현한다.
**이 테스트가 실패하면 다른 테스트는 의미 없다.**

### Baseline Test — 정상 부하에서 얼마나 빠른가?

VU 30명이 정상 트래픽 패턴(조회 80% + 입찰 20%)으로 5분간 서비스를 사용한다.
측정한 p90/p99를 이후 테스트의 비교 기준선으로 사용한다.
"아무 문제 없는 상황"의 응답시간을 기록해두는 것이 목적이다.

### Concurrent Bid Test — 동시 입찰 시 무결성이 보장되는가?

이 서비스의 핵심 테스트다.
N명이 동시에 같은 경매에 입찰할 때 비관적 락(PESSIMISTIC_WRITE)이 올바르게 작동하는지 검증한다.

테스트 후 반드시 DB에서 무결성 쿼리를 실행해야 한다 (handleSummary에 자동 출력됨):
- PENDING 중복 생성 여부
- ACTIVE 중복(중복 낙찰) 여부
- 낙찰가 역전 여부

`bid_server_error = 0` 이면 동시성 처리 정상, `bid_server_error > 0` 이면 락 구현 문제.

### Load Test — 실제 사용 패턴에서 안정적인가?

조회 70% + 입찰 30% 비율로 실제 서비스 이용 패턴을 재현한다.
50 VU까지 점진적으로 올리면서 응답시간이 baseline 대비 얼마나 변화하는지 측정한다.
"출시 후 일반 사용 상황"을 시뮬레이션한다.

### Spike Test — 갑작스러운 폭발 트래픽에 버티는가?

경매 마감 직전 입찰 러시처럼 트래픽이 갑자기 10배 증가하는 상황을 재현한다.
스파이크(100 VU, 30초) 동안의 서버 오류율과 응답시간 변화를 측정하고,
스파이크 해소 후 정상으로 회복되는 시간을 확인한다.

### Stress Test — 한계점이 어디인가?

VU를 20 → 50 → 100 → 200으로 단계적으로 올리면서 에러율 5% 초과 시점을 탐색한다.
"이 시스템이 버틸 수 있는 최대 동시 사용자 수"를 수치로 파악하는 것이 목적이다.
한계 탐색이 목적이므로 임계값을 느슨하게 설정한다 — 통과가 목표가 아니다.

### Soak Test — 오래 사용하면 문제가 생기는가?

낮은 부하(20 VU)를 30분~수 시간 유지하면서 시간이 지날수록 나타나는 이상 징후를 감지한다.
메모리 누수, DB 커넥션 누수, Kafka 이벤트 누적은 k6만으로는 볼 수 없으므로
**Grafana에서 JVM Heap / HikariCP Active / Kafka consumer lag을 반드시 병행 모니터링**해야 한다.

---

## 실행 환경

| 항목 | 값 |
|------|----|
| 테스트 실행 위치 | EC2 서버 (`ubuntu@3.36.235.7`) |
| k6 스크립트 경로 | `~/k6/scenarios/` |
| auction 서비스 | K8s ClusterIP `http://10.43.214.112:8090` |
| member 서비스 | K8s ClusterIP `http://10.43.207.139:8083` |
| 접근 방식 | Gateway 우회, ClusterIP 직접 접근 |

## 시스템 특성 (테스트 설계 근거)

| 항목 | 값 | 의미 |
|------|----|------|
| HikariCP pool | min 2 / max 5 | 예상 병목 — 동시 입찰 5건 초과 시 대기 발생 |
| 입찰 락 방식 | PESSIMISTIC_WRITE | 직렬화로 정합성 보장, 처리량 제한 |
| 입찰 확정 방식 | Outbox → Kafka → Payment → 확정 | 비동기 구조, 201 응답 후에도 PENDING 상태 유지 |
| 스케줄러 주기 | 10초 | 경매 상태 전이 자동화 |

## 인증 방식

Gateway 우회 직접 접근 시 X-헤더 설정 (common-security 규약):

```
X-Member-Id: {UUID}
X-Member-Role: USER | SELLER | ADMIN
X-Session-Id: {UUID}
```

---

## 사전 준비

### 1. 최초 1회 실행 (데이터 삽입)

```bash
PSQL="kubectl exec -i <postgres-pod> -n goods-mall -- psql -U goods -d goods_mall"
$PSQL < ~/k6/seed/cleanup_test_data.sql
$PSQL < ~/k6/seed/seed_test_auctions.sql
$PSQL < ~/k6/seed/seed_baseline_wallets.sql
$PSQL < ~/k6/seed/load_test_auctions.sql
```

또는 `setup.sh` 실행:

```bash
chmod +x ~/k6/setup.sh && ~/k6/setup.sh
```

### 2. 매 시나리오 전 DB 리셋

```bash
$PSQL < ~/k6/seed/reset_test_auctions.sql
```

입찰이 누적되면 `current_highest_price`가 올라가 이후 시나리오의 입찰가 계산 기준이 달라지므로 반드시 리셋해야 한다.

### 3. 로컬 → EC2 파일 업로드

```powershell
scp -r -i <pem파일> auction\k6\ ubuntu@3.36.235.7:~/k6/
```

---

## 테스트 실행 순서

### 전체 자동 실행

```bash
chmod +x ~/k6/run_all.sh && ~/k6/run_all.sh
```

### 개별 실행

```bash
# 1. 기능 검증
k6 run ~/k6/scenarios/smoke.js

# 2. 기준선 측정
k6 run ~/k6/scenarios/baseline.js

# 3. 동시 입찰 (핵심 — VU별 단계 실행 권장)
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=30
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=50
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=100

# 4. 일반 부하
k6 run ~/k6/scenarios/load.js

# 5. 스파이크
k6 run ~/k6/scenarios/spike.js

# 6. 스트레스 (한계점 탐색)
k6 run ~/k6/scenarios/stress.js

# 7. 장기 안정성
k6 run ~/k6/scenarios/soak.js -e DURATION=30m -e VUS=20
```

### Grafana 연동 실행

```bash
k6 run --out influxdb=http://localhost:30086/k6 ~/k6/scenarios/concurrent-bid.js -e VUS=50
```

---

## 전체 실행 체크리스트

```
[ ] setup.sh 실행 확인 (최초 1회)
[ ] Grafana 대시보드 열기 (soak 시에는 필수)

[ ] smoke      → 기능 검증 통과 확인
[ ] reset DB
[ ] baseline   → p90/p99 기준선 기록
[ ] reset DB
[ ] concurrent-bid VUS=30 → DB 무결성 쿼리 실행
[ ] reset DB
[ ] concurrent-bid VUS=50 → DB 무결성 쿼리 실행
[ ] reset DB
[ ] load       → 혼합 부하 안정성 확인
[ ] reset DB
[ ] spike      → 스파이크 내성 및 회복 확인
[ ] reset DB
[ ] stress     → 포화점 VU 수 기록
[ ] reset DB

[ ] wallet 잔액 확인 (SELECT balance FROM payment.wallet ...)
    → 부족하면 refill_wallet.sql 실행
[ ] soak       → 장기 누수 여부 확인 (Grafana 병행)

[ ] 종료 후 cleanup_test_data.sql 실행
```

---

## 예상 병목 & 개선 포인트

| 병목 | 원인 | 개선 방안 |
|------|------|-----------|
| HikariCP pool 소진 | max-pool-size=5 | 10~20으로 증가 후 재측정 |
| 입찰 직렬화 처리 | PESSIMISTIC_WRITE 락 | Redis 분산 락 검토 |
| 조회 성능 | 인덱스 미활용 | EXPLAIN ANALYZE 후 인덱스 추가 |
| WebSocket 확장성 | 인메모리 상태 | Redis pub/sub 연동 검토 |
