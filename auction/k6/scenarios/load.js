/**
 * 시나리오 B: 부하 테스트 (Load Test)
 * 목적: 실제 사용 패턴(읽기 70% + 입찰 30%) 에서 안정성 측정
 * 실행: k6 run auction/k6/scenarios/load.js --out json=results/load.json
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS } from '../helpers/data.js';
import { publicHeaders } from '../helpers/auth.js';

const bidTrend = new Trend('load_bid_duration', true);
const readTrend = new Trend('load_read_duration', true);
const errorRate = new Rate('load_error_rate');

export const options = {
  stages: [
    { duration: '3m', target: 50 },   // ramp-up
    { duration: '7m', target: 100 },  // 목표 부하 유지
    { duration: '2m', target: 0 },    // ramp-down
  ],
  thresholds: {
    load_bid_duration: ['p(90)<500', 'p(99)<2000'],
    load_read_duration: ['p(90)<300', 'p(99)<1000'],
    load_error_rate: ['rate<0.01'],
    http_req_failed: ['rate<0.05'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.40) {
    // 40% 경매 목록 조회
    const res = http.get(
      `${BASE_URL}/api/auctions?status=ONGOING&page=0&size=9`,
      { headers: publicHeaders() }
    );
    readTrend.add(res.timings.duration);
    errorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'list 200': (r) => r.status === 200 });
    sleep(0.3);

  } else if (rand < 0.70) {
    // 30% 경매 상세 조회
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    readTrend.add(res.timings.duration);
    errorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'detail 200': (r) => r.status === 200 });
    sleep(0.2);

  } else {
    // 30% 입찰
    const memberId = uuidv4();
    const res = http.post(
      `${BASE_URL}/api/auctions/${auctionId}/bids`,
      JSON.stringify({ bidPrice: 50000 }),
      {
        headers: {
          'Content-Type': 'application/json',
          'X-Member-Id': memberId,
          'X-Member-Role': 'BUYER',
          'X-Session-Id': uuidv4(),
        },
      }
    );
    bidTrend.add(res.timings.duration);
    errorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'bid 5xx 없음': (r) => r.status < 500 });
    sleep(1);
  }
}
