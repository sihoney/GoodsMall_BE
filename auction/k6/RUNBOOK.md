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

baseline 입찰자 10명 wallet 생성 (member_id `11111111-...-11111111111[0-9]`, 잔액 5,000,000):

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
