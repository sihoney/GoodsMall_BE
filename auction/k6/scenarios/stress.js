/**
 * 시나리오 D: 스트레스 테스트 (Stress Test)
 * 목적: 시스템 포화점(Saturation Point) 탐색
 *       에러율 5% 초과 시점 = 한계점
 * 실행: k6 run auction/k6/scenarios/stress.js --out json=results/stress.json
 * 전제: reset_test_auctions.sql 실행 후 시작
 *
 * VU 스케일 근거 (HikariCP max=5):
 *   50 VU → 이미 DB 커넥션 경합 시작
 *   100 VU → 포화 예상 구간
 *   200 VU → 실질적 한계점 확인
 */
import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS } from '../helpers/data.js';
import { publicHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const errorRate = new Rate('stress_error_rate');
const bidTrend = new Trend('stress_bid_duration', true);

export const options = {
  stages: [
    { duration: '2m', target: 20 },
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 0 },  // 회복
  ],
  thresholds: {
    // 임계값을 느슨하게 설정 (한계점 탐색이 목적)
    stress_error_rate: ['rate<0.50'],
    http_req_failed: ['rate<0.50'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.40) {
    const res = http.get(
      `${BASE_URL}/api/auctions?page=0&size=9`,
      { headers: publicHeaders() }
    );
    errorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'list ok': (r) => r.status < 500 });

  } else if (rand < 0.70) {
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    errorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'detail ok': (r) => r.status < 500 });

  } else {
    // 30% 입찰 (현재 최고가 기반 동적 bidPrice)
    const bidPrice = fetchCurrentBidPrice(auctionId);
    if (bidPrice !== null) {
      const res = http.post(
        `${BASE_URL}/api/auctions/${auctionId}/bids`,
        JSON.stringify({ bidPrice }),
        {
          headers: {
            'Content-Type': 'application/json',
            'X-Member-Id': uuidv4(),
            'X-Member-Role': 'BUYER',
            'X-Session-Id': uuidv4(),
          },
        }
      );
      bidTrend.add(res.timings.duration);
      errorRate.add(res.status >= 500 ? 1 : 0);
      check(res, { 'bid ok': (r) => r.status < 500 });
    }
  }
}
