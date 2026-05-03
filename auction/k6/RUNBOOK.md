# 부하테스트 실행 순서

## 사전 준비 — EC2에 스크립트 복사

**PowerShell** / 프로젝트 루트(`beadv5_2_TodayLunchMenu_BE`)에서 실행:

```powershell
scp -r auction\k6\* ubuntu@3.36.235.7:~/k6/
```

EC2 접속:

```powershell
ssh -i ~/.ssh/id_rsa ubuntu@3.36.235.7
```

---

## STEP 1 — Smoke Test

wallet 시딩 불필요 (기존 시드 멤버 buyer101/102 사용)

테스트 경매 시딩 (최초 1회 — eeeeeeee-...-001~003 이 없을 때):

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/seed_test_auctions.sql
```

경매 상태 초기화:

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
```

smoke 실행:

```bash
k6 run ~/k6/scenarios/smoke.js
```

**통과 기준**: 5xx 0%, checks 100%

---

## STEP 2 — Baseline 전용 Wallet 시딩 (최초 1회)

baseline 입찰자 30명 wallet 생성 (member_id `11111111-...-11111111111[0-9]` ~ `11111111-...-11111111113[0-9]`, 잔액 5,000,000):

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/seed_baseline_wallets.sql
```

---

## STEP 3 — Baseline Test

경매 상태 초기화:

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
```

baseline 실행:

```bash
k6 run ~/k6/scenarios/baseline.js
```

**통과 기준**: bid p90 < 500ms, p99 < 2000ms, 5xx 0%  
**확인 포인트**: `bid_success` / `bid_rejected` 카운터 수치 기록

---

## STEP 4 — Load / Stress / Spike / Soak / Concurrent-Bid 공통 사전 작업 (최초 1회)

load 이후 시나리오는 `LOAD_TEST_AUCTION_IDS` (aaaaaaaa-...-101~105) 와 `CONCURRENT_BID_AUCTION_ID` (...-201) 를 사용한다. 별도 시드 SQL 적용 필요:

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/load_test_auctions.sql
```

이 SQL은 ON CONFLICT DO NOTHING 이라 중복 실행해도 안전.

> 입찰자는 STEP 2 에서 시드한 `BASELINE_BIDDER_IDS` (30명) 풀을 그대로 재사용한다.
> 잔액 소진 시 아래 "잔액 소진 시" 섹션의 `refill_wallet.sql` 실행.

---

## STEP 5 — Load Test

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
k6 run ~/k6/scenarios/load.js
```

**통과 기준**: load_bid_duration p99<2000ms, load_read_duration p99<1000ms, load_error_rate(5xx)<1%, http_req_failed<5%

---

## STEP 6 — Stress Test (한계점 탐색)

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
k6 run ~/k6/scenarios/stress.js
```

**통과 기준**: stress_status_500 == 0 (코드 버그 검출)  
**관찰 포인트**: stress_5xx_rate 가 5% 초과하는 VU 구간이 한계점. 503/504/500 분포로 원인 진단.

---

## STEP 7 — Spike Test (입찰 러시 재현)

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
k6 run ~/k6/scenarios/spike.js
```

**통과 기준**: spike_error_rate < 10%, http_req_failed < 10%  
**관찰 포인트**: 스파이크 종료 후 응답시간 회복 시간

---

## STEP 8 — Concurrent-Bid Test (락 경합)

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=30
```

**통과 기준**: successful_bids > 0, bid_duration p99 < 5000ms, 5xx 0건  
**확인 포인트**: pending_overrun 분석 시 -1 보정 (실제 ACTIVE는 1건)  
**주의**: VUS > 30 이면 입찰자 풀(30명) modulo 중복 → 422 증가로 락 측정 정확도 하락

---

## STEP 9 — Soak Test (장기 안정성)

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/reset_test_auctions.sql
k6 run ~/k6/scenarios/soak.js                              # 기본 30분
k6 run ~/k6/scenarios/soak.js -e DURATION=4h -e VUS=30     # 누수 검증용
```

**통과 기준**: soak_error_rate < 2%, http_req_duration p99 < 3000ms  
**확인 포인트**: Grafana 에서 JVM heap, HikariCP active, Kafka consumer lag 동시 추적

---

## STEP 10 — WebSocket Test (실시간 브로드캐스트)

```bash
k6 run ~/k6/scenarios/websocket.js -e CONNECTIONS=200
```

**통과 기준**: ws_connection_error_rate < 5%, ws_messages_received > 0  
**전제**: STOMP 엔드포인트가 ClusterIP 8090 으로 노출되어 있어야 함 (config/thresholds.js의 BASE_URL 자동 파생)

---

## 잔액 소진 시 (재충전)

기존 시드 멤버(buyer101~103, seller) + baseline 입찰자 10명 잔액 복원:

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/refill_wallet.sql
```

---

## 전체 테스트 종료 후 (데이터 정리)

ROLLBACK 상태로 실행 → SELECT 결과 확인:

```bash
kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall < ~/k6/seed/cleanup_test_data.sql
```

결과 확인 후 파일 맨 아래 `ROLLBACK` → `COMMIT` 으로 변경 후 재실행
