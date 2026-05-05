#!/bin/bash
set -e

PSQL="kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall"
K6_DIR=~/k6

echo "===== [1/4] 기존 테스트 데이터 클린업 ====="
$PSQL < $K6_DIR/seed/cleanup_test_data.sql

echo "===== [2/4] 테스트 경매 시딩 (smoke/baseline용) ====="
$PSQL < $K6_DIR/seed/seed_test_auctions.sql

echo "===== [3/4] 베이스라인 입찰자 wallet 시딩 (30명) ====="
$PSQL < $K6_DIR/seed/seed_baseline_wallets.sql

echo "===== [4/4] load/stress/spike/soak/concurrent-bid 경매 시딩 ====="
$PSQL < $K6_DIR/seed/load_test_auctions.sql

echo "===== 셋업 완료 — run_all.sh 를 실행하세요 ====="
