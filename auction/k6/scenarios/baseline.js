/**
 * 기준선 측정 (Baseline Test)
 * 목적: 정상 부하에서 p50/p90/p99 응답시간 측정 → 이후 테스트 비교 기준
 * 실행: k6 run auction/k6/scenarios/baseline.js --out json=results/baseline.json
 * 전제: reset_test_auctions.sql 실행 후 시작
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { buyerHeaders, publicHeaders } from '../helpers/auth.js';
import { SEED_AUCTIONS, randomAuctionId } from '../helpers/data.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const bidDuration = new Trend('bid_duration', true);
const listDuration = new Trend('list_duration', true);
const detailDuration = new Trend('detail_duration', true);

export const options = {
  stages: [
    { duration: '1m', target: 10 },  // warm-up
    { duration: '3m', target: 30 },  // 기준 부하
    { duration: '1m', target: 0 },   // cool-down
  ],
  thresholds: {
    bid_duration: ['p(90)<500', 'p(99)<2000'],
    list_duration: ['p(90)<300', 'p(99)<1000'],
    detail_duration: ['p(90)<200', 'p(99)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const rand = Math.random();

  if (rand < 0.5) {
    // 50% 경매 목록 조회
    const res = http.get(
      `${BASE_URL}/api/auctions?status=ONGOING&page=0&size=9`,
      { headers: publicHeaders() }
    );
    listDuration.add(res.timings.duration);
    check(res, { '목록 조회 200': (r) => r.status === 200 });

  } else if (rand < 0.8) {
    // 30% 경매 상세 조회
    const res = http.get(
      `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}`,
      { headers: publicHeaders() }
    );
    detailDuration.add(res.timings.duration);
    check(res, { '상세 조회 200': (r) => r.status === 200 });

  } else {
    // 20% 입찰 시도 (현재 최고가 기반 동적 bidPrice)
    const bidPrice = fetchCurrentBidPrice(SEED_AUCTIONS.ONGOING);
    if (bidPrice !== null) {
      const res = http.post(
        `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}/bids`,
        JSON.stringify({ bidPrice }),
        { headers: buyerHeaders() }
      );
      bidDuration.add(res.timings.duration);
      check(res, { '입찰 5xx 없음': (r) => r.status < 500 });
    }
  }

  sleep(0.5);
}
