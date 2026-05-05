#!/bin/bash
set -e

PSQL="kubectl exec -i postgres-c99f56b7d-7bdts -n goods-mall -- psql -U goods -d goods_mall"
K6_DIR=~/k6
INFLUXDB="influxdb=http://localhost:30086/k6"

echo "===== [1/7] Smoke Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/smoke.js

echo "===== [2/7] Baseline Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/baseline.js

echo "===== [3/7] Load Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/load.js

echo "===== [4/7] Stress Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/stress.js

echo "===== [5/7] Spike Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/spike.js

echo "===== [6/7] Concurrent-Bid Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/concurrent-bid.js -e VUS=30

echo "===== [7/7] Soak Test ====="
$PSQL < $K6_DIR/seed/reset_test_auctions.sql
k6 run --out $INFLUXDB $K6_DIR/scenarios/soak.js

echo "===== 전체 완료 ====="
