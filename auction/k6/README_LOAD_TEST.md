# 경매 서비스 부하테스트 가이드

## 시스템 특성 (테스트 설계 근거)

| 항목 | 값 | 의미 |
|------|----|------|
| 포트 | 8090 | 직접 접근 (게이트웨이 우회) |
| HikariCP pool | min 2 / max 5 | **예상 병목** — 동시 입찰 5건 초과 시 대기 발생 |
| 입찰 락 방식 | PESSIMISTIC_WRITE | 직렬화로 정합성 보장, 처리량 제한 |
| 입찰 확정 방식 | Outbox → Kafka → Payment → 확정 | 비동기 구조, 부하테스트 중 Payment 없으면 PENDING 유지 |
| 스케줄러 주기 | 10초 | 경매 상태 전이 자동화 |

## 인증 방식

게이트웨이 없이 직접 테스트 시 헤더만 설정:
```
X-Member-Id: {UUID}
X-Member-Role: BUYER | SELLER
X-Session-Id: {UUID}
```

## 사전 준비

### 1. k6 설치
```bash
winget install k6
```

### 2. 모니터링 스택 시작
```bash
docker compose -f docker-compose.local.yml up prometheus grafana -d
```
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin)
- Grafana에서 JVM Micrometer 대시보드 import (ID: 4701)

### 3. 경매 서비스 시작
```bash
# 로컬 직접 실행 (IntelliJ 또는)
./gradlew :auction:bootRun
```

### 4. 부하테스트 전용 데이터 삽입
```bash
psql -U postgres -d goods_mall -f auction/k6/seed/load_test_auctions.sql
```

---

## 테스트 실행 순서 (하루 계획)

### 오전 (2h): 검증 + 기준선

```bash
# 스모크 테스트 (기능 검증)
k6 run auction/k6/scenarios/smoke.js

# 기준선 측정
k6 run auction/k6/scenarios/baseline.js --out json=auction/k6/results/baseline.json
```

### 오전~오후 (4h): 핵심 시나리오

```bash
# 시나리오 A: 동시 입찰 (3단계)
k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=50  --out json=auction/k6/results/concurrent-bid-50.json
k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=100 --out json=auction/k6/results/concurrent-bid-100.json
k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=200 --out json=auction/k6/results/concurrent-bid-200.json

# 시나리오 B: 부하 테스트 (읽기 70% + 입찰 30%)
k6 run auction/k6/scenarios/load.js --out json=auction/k6/results/load.json

# 시나리오 C: 스파이크 (경매 마감 러시)
k6 run auction/k6/scenarios/spike.js --out json=auction/k6/results/spike.json

# 시나리오 D: 스트레스 (한계점 탐색)
k6 run auction/k6/scenarios/stress.js --out json=auction/k6/results/stress.json
```

### 오후 (2h): 안정성 + 분석

```bash
# 시나리오 E: 장기 안정성 (30분)
k6 run auction/k6/scenarios/soak.js -e DURATION=30m -e VUS=50 --out json=auction/k6/results/soak.json

# WebSocket 동시 연결
k6 run auction/k6/scenarios/websocket.js -e CONNECTIONS=200 --out json=auction/k6/results/websocket.json
```

---

## 측정 지표 & 성공 기준

| 지표 | 목표 | 경고 | 위험 |
|------|------|------|------|
| p90 응답시간 (입찰) | < 500ms | 500~1s | > 1s |
| p99 응답시간 (입찰) | < 2s | 2~5s | > 5s |
| 에러율 (5xx) | < 1% | 1~5% | > 5% |
| 입찰 처리량 | > 50 RPS | 20~50 | < 20 |
| HikariCP 대기 | 없음 | 간헐적 | 지속 |

---

## 주요 관찰 포인트

### Grafana에서 확인할 메트릭
- `hikaricp_connections_pending` — 커넥션 대기 수
- `jvm_memory_used_bytes` — 힙 메모리 증가 추이
- `jvm_gc_pause_seconds` — GC 빈도
- `http_server_requests_seconds` — 엔드포인트별 응답시간

### Kafka UI (localhost:8089)
- `auction.bid-fee.charge.requested` 토픽 Consumer lag
- 부하 테스트 중 Payment 없으면 lag 무한 증가 (예상 동작)

---

## 예상 병목 & 개선 포인트

| 병목 | 원인 | 개선 방안 |
|------|------|-----------|
| HikariCP pool 소진 | max-pool-size=5 | 10~20으로 증가 후 재측정 |
| 입찰 직렬화 처리 | PESSIMISTIC_WRITE 락 | Redis 분산 락 검토 |
| 조회 성능 | 인덱스 미활용 | EXPLAIN ANALYZE 후 인덱스 추가 |
| WebSocket 확장성 | 인메모리 상태 | Redis pub/sub 연동 검토 |

---

## 결과 해석 가이드

```bash
# JSON 결과 요약 조회
cat auction/k6/results/load.json | jq '[.metrics.http_req_duration.values | to_entries[] | select(.key | startswith("p("))]'
```

각 시나리오 결과를 표로 정리 후 포트폴리오 문서에 첨부:
1. 스크린샷: Grafana 대시보드
2. 표: 시나리오별 p90/p99/에러율/RPS
3. 그래프: HikariCP pool-size 변경 전후 비교
