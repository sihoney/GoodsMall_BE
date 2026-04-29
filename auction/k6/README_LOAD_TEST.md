# 경매 서비스 부하테스트 가이드

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
| HikariCP pool | min 2 / max 5 | **예상 병목** — 동시 입찰 5건 초과 시 대기 발생 |
| 입찰 락 방식 | PESSIMISTIC_WRITE | 직렬화로 정합성 보장, 처리량 제한 |
| 입찰 확정 방식 | Outbox → Kafka → Payment → 확정 | 비동기 구조, Payment 없으면 PENDING 유지 |
| 스케줄러 주기 | 10초 | 경매 상태 전이 자동화 |

## 인증 방식

Gateway 우회 직접 접근 시 X-헤더 설정 (common-security 규약):

```
X-Member-Id: {UUID}
X-Member-Role: USER | SELLER | ADMIN    ← BUYER 아님 주의
X-Session-Id: {UUID}
```

## 사전 준비

### 1. 시드 데이터 삽입 (K8s postgres pod)

```bash
# postgres pod 확인
kubectl get pods -n goods-mall | grep postgres

# 테스트 계정 삽입 (dev_seed_member.sql 기준)
kubectl exec -it <postgres-pod> -n goods-mall -- psql -U postgres -d goods_mall

# 아래 SQL 실행:
# 1) member (buyer@test.local / seller@test.local)
# 2) wallet (각 계정 100만원)
# 3) load_test_auctions.sql (부하테스트 경매 데이터)
```

필요한 계정 데이터:
```sql
-- member 계정
INSERT INTO member.member (member_id, email, password, nickname, phone, address, profile_image_key, role, status, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111101','buyer@test.local','$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u','김구매','010-1111-1111','서울시 강남구',NULL,'USER','ACTIVE',NOW(),NOW()),
  ('11111111-1111-1111-1111-111111111102','buyer2@test.local','$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u','박입찰','010-1111-1112','서울시 서초구',NULL,'USER','ACTIVE',NOW(),NOW()),
  ('11111111-1111-1111-1111-111111111103','buyer3@test.local','$2b$10$yXUMG19nj3DhnhHmlM8hqOOlfnN7zPJkEn9zoKFdIVExwazLwwr0u','최경매','010-1111-1113','서울시 송파구',NULL,'USER','ACTIVE',NOW(),NOW()),
  ('22222222-2222-2222-2222-222222222202','seller@test.local','$2b$10$2WSJsxM.EYyBp0WC7Of5hehZDI18sL897M0SAsuq5yCR06IgsX7Jy','이판매','010-2222-2222','서울시 마포구',NULL,'SELLER','ACTIVE',NOW(),NOW())
ON CONFLICT (member_id) DO NOTHING;

-- seller 등록
INSERT INTO member.seller (seller_id, member_id, bank_name, account, approved_at)
VALUES ('44444444-4444-4444-4444-444444444404','22222222-2222-2222-2222-222222222202','국민은행','123-456-7890',NOW())
ON CONFLICT (member_id) DO NOTHING;

-- wallet (입찰 예치금)
INSERT INTO payment.wallet (wallet_id, member_id, balance, created_at, updated_at)
VALUES
  ('aaaaaaaa-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111101', 1000000.00, NOW(), NOW()),
  ('aaaaaaaa-1111-1111-1111-111111111102', '11111111-1111-1111-1111-111111111102', 1000000.00, NOW(), NOW()),
  ('aaaaaaaa-1111-1111-1111-111111111103', '11111111-1111-1111-1111-111111111103', 1000000.00, NOW(), NOW()),
  ('aaaaaaaa-2222-2222-2222-222222222202', '22222222-2222-2222-2222-222222222202', 1000000.00, NOW(), NOW())
ON CONFLICT (member_id) DO NOTHING;
```

### 2. 부하테스트 전용 경매 데이터 삽입

```bash
# EC2 서버에서 직접 실행 (postgres pod에서)
kubectl exec -it <postgres-pod> -n goods-mall -- psql -U postgres -d goods_mall -f /path/to/load_test_auctions.sql
```

또는 `~/k6/seed/load_test_auctions.sql` 내용을 postgres pod에서 직접 실행.

### 3. 파일 업로드 (로컬 → EC2)

```powershell
# Windows PowerShell (프로젝트 루트에서)
scp -r -i $env:USERPROFILE\.ssh\id_rsa auction\k6\ ubuntu@3.36.235.7:~/k6_upload/
```

```bash
# EC2에서 복사 (중첩 폴더 방지)
cp -r ~/k6_upload/* ~/k6/ && rm -rf ~/k6_upload
```

---

## 테스트 실행 순서

### 1단계: 스모크 테스트 (기능 검증)

```bash
k6 run ~/k6/scenarios/smoke.js
```

### 2단계: 기준선 측정

```bash
k6 run ~/k6/scenarios/baseline.js
```

### 3단계: 핵심 시나리오

```bash
# 시나리오 A: 동시 입찰 (락 경합 테스트)
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=50
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=100
k6 run ~/k6/scenarios/concurrent-bid.js -e VUS=200

# 시나리오 B: 부하 테스트 (읽기 70% + 입찰 30%)
k6 run ~/k6/scenarios/load.js

# 시나리오 C: 스파이크
k6 run ~/k6/scenarios/spike.js

# 시나리오 D: 스트레스
k6 run ~/k6/scenarios/stress.js
```

### 4단계: 장기 안정성

```bash
k6 run ~/k6/scenarios/soak.js -e DURATION=30m -e VUS=50
```

---

## 측정 지표 & 성공 기준

| 지표 | 목표 | 경고 | 위험 |
|------|------|------|------|
| p90 응답시간 (입찰) | < 500ms | 500ms~1s | > 1s |
| p99 응답시간 (입찰) | < 2s | 2~5s | > 5s |
| 에러율 (5xx) | < 1% | 1~5% | > 5% |
| 입찰 처리량 | > 50 RPS | 20~50 RPS | < 20 RPS |
| HikariCP 대기 | 없음 | 간헐적 | 지속 |

---

## 예상 병목 & 개선 포인트

| 병목 | 원인 | 개선 방안 |
|------|------|-----------|
| HikariCP pool 소진 | max-pool-size=5 | 10~20으로 증가 후 재측정 |
| 입찰 직렬화 처리 | PESSIMISTIC_WRITE 락 | Redis 분산 락 검토 |
| 동시성 룰 우회 | currentHighestPrice 비동기 갱신 | `BidCreateService.place()` 락 안에서 즉시 갱신 (Notion 분석 노트 참조) |
| 조회 성능 | 인덱스 미활용 | EXPLAIN ANALYZE 후 인덱스 추가 |
| WebSocket 확장성 | 인메모리 상태 | Redis pub/sub 연동 검토 |
