/**
 * 시나리오 A: 동시 입찰 경쟁 테스트 (Concurrent Bid Test)
 * 목적: PESSIMISTIC_WRITE 락 경합 시 처리량 한계 측정
 *       HikariCP pool(max=5) 소진 시점 확인
 *
 * 락 경합 원리:
 *   여러 VU가 동시에 경매 상세 조회 → 같은 currentHighestPrice 읽음
 *   → 동일 bidPrice로 동시 POST → DB 락 경합 발생 → 1건 성공, 나머지 409
 *   bid_conflict_rate = 409 비율 (락 경합 지표)
 *
 * 사전 조건:
 *   1) load_test_auctions.sql 실행
 *   2) reset_test_auctions.sql 실행 (current_highest_price = NULL 초기화)
 *
 * 실행:
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=30
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=50
 *   k6 run auction/k6/scenarios/concurrent-bid.js -e VUS=100
 */
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { CONCURRENT_BID_AUCTION_ID } from '../helpers/data.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const successfulBids = new Counter('successful_bids');
const failedBids = new Counter('failed_bids');
const bidConflictRate = new Rate('bid_conflict_rate'); // 409 = 동시 입찰 경합
const bidTrend = new Trend('bid_duration', true);

const TARGET_VUS = parseInt(__ENV.VUS || '30');

export const options = {
  scenarios: {
    concurrent_bid: {
      executor: 'constant-vus',
      vus: TARGET_VUS,
      duration: '2m',
    },
  },
  thresholds: {
    successful_bids: ['count>0'],
    http_req_failed: ['rate<0.50'],
  },
};

export default function () {
  // 각 VU가 고유한 memberId 사용 (같은 최고입찰자 재입찰 금지 정책 우회)
  const memberId = uuidv4();

  // 현재 최고가 조회 — 여러 VU가 동시에 같은 값을 읽으면 동일 bidPrice 계산 → 락 경합 발생
  const bidPrice = fetchCurrentBidPrice(CONCURRENT_BID_AUCTION_ID);
  if (bidPrice === null) {
    console.log('경매 상세 조회 실패, 입찰 스킵');
    return;
  }

  const res = http.post(
    `${BASE_URL}/api/auctions/${CONCURRENT_BID_AUCTION_ID}/bids`,
    JSON.stringify({ bidPrice }),
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

  const isSuccess = res.status === 201;
  const isBidConflict = res.status === 409; // 동시 입찰로 인한 가격 충돌

  if (isSuccess) {
    successfulBids.add(1);
  } else {
    failedBids.add(1);
  }
  bidConflictRate.add(isBidConflict ? 1 : 0);

  if (res.status >= 500) {
    console.log(`SERVER ERROR: status=${res.status} body=${res.body}`);
  }

  check(res, {
    '서버 에러 없음': (r) => r.status < 500,
    '입찰 처리됨 (성공 또는 비즈니스 에러)': (r) => r.status < 500,
  });
}
