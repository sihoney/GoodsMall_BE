/**
 * 시나리오 A: 동시 입찰 경쟁 테스트 (Concurrent Bid Test)
 * 목적: PESSIMISTIC_WRITE 락 경합 시 처리량 한계 측정
 *       HikariCP pool(max=5) 소진 시점 확인
 *
 * 사전 조건: load_test_auctions.sql 실행 필요
 *   - CONCURRENT_BID_AUCTION_ID 경매가 ONGOING 상태이고 currentHighestPrice=null
 *
 * 실행:
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=50 --out json=results/concurrent-bid-50.json
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=100 --out json=results/concurrent-bid-100.json
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=200 --out json=results/concurrent-bid-200.json
 */
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { CONCURRENT_BID_AUCTION_ID } from '../helpers/data.js';

const successfulBids = new Counter('successful_bids');
const failedBids = new Counter('failed_bids');
const lockContentionRate = new Rate('lock_contention');
const bidP99 = new Trend('bid_p99', true);

const TARGET_VUS = parseInt(__ENV.VUS || '50');
const BID_PRICE = 50000; // startPrice (currentHighest=null이므로 startPrice 이상이면 유효)

export const options = {
  scenarios: {
    concurrent_bid: {
      executor: 'constant-vus',
      vus: TARGET_VUS,
      duration: '2m',
    },
  },
  thresholds: {
    successful_bids: [`count>0`],
    http_req_failed: ['rate<0.50'], // 비즈니스 에러(락 경합)는 실패로 간주하지 않음
  },
};

export default function () {
  // 각 VU가 고유한 memberId를 사용 (같은 최고입찰자 재입찰 금지 정책 우회)
  const memberId = uuidv4();

  const headers = {
    'Content-Type': 'application/json',
    'X-Member-Id': memberId,
    'X-Member-Role': 'BUYER',
    'X-Session-Id': uuidv4(),
  };

  const res = http.post(
    `${BASE_URL}/api/auctions/${CONCURRENT_BID_AUCTION_ID}/bids`,
    JSON.stringify({ bidPrice: BID_PRICE }),
    { headers }
  );

  bidP99.add(res.timings.duration);

  const isSuccess = res.status === 201;
  const isBusinessError = [400, 409, 422].includes(res.status);
  const isServerError = res.status >= 500;

  if (isSuccess) {
    successfulBids.add(1);
  } else {
    failedBids.add(1);
  }

  // 5xx는 락 경합이 아닌 실제 오류
  lockContentionRate.add(isServerError ? 1 : 0);

  check(res, {
    '서버 에러 없음': (r) => r.status < 500,
    '입찰 처리됨 (성공 또는 비즈니스 에러)': (r) => r.status < 500,
  });
}
