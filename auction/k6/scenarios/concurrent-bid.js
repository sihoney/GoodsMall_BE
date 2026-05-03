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
import { Counter, Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { CONCURRENT_BID_AUCTION_ID, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { buyerHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const successfulBids = new Counter('successful_bids');
const failedBids = new Counter('failed_bids');
// 락은 잡히지만 currentHighestPrice 갱신이 Payment Kafka 응답 후라서 동시 입찰이 모두 PENDING 통과한다.
// 한 경매당 최종 ACTIVE는 1건이어야 정상이므로, 그 외 PENDING 성공은 모두 "낭비된 Payment 호출/wallet 차감".
// 분석 시 정확한 값 = successful_bids - 1 (테스트 시작 시점 currentHighestPrice=NULL 전제).
const pendingOverrun = new Counter('pending_overrun');
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
    // 락 직렬화로 인한 꼬리 지연 한계 — 한계점 탐색 목적이라 느슨히 잡음
    bid_duration: ['p(99)<5000', 'p(99.9)<10000'],
    http_req_failed: ['rate<0.50'],
  },
};

export default function () {
  // VU별 고정 입찰자 — wallet 보유 + 동일 입찰자 충돌 회피 (HIGHEST_BIDDER_CANNOT_REBID 룰 우회)
  // 풀(30명)을 초과하는 VU 수에서는 modulo로 중복 사용되어 422가 발생할 수 있음
  // → 락 직렬화 측정의 정확도가 떨어지므로 VUS<=30 권장
  const bidderId = BASELINE_BIDDER_IDS[(__VU - 1) % BASELINE_BIDDER_IDS.length];

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
      headers: buyerHeaders(bidderId),
      responseCallback: http.expectedStatuses(201, 400, 409, 422),
    }
  );

  bidTrend.add(res.timings.duration);

  const isSuccess = res.status === 201;

  if (isSuccess) {
    successfulBids.add(1);
    pendingOverrun.add(1); // 분석 시 -1 보정 (실제 ACTIVE는 1건)
  } else {
    failedBids.add(1);
  }

  if (res.status >= 500) {
    console.log(`SERVER ERROR: status=${res.status} body=${res.body}`);
  }

  check(res, {
    '서버 에러 없음': (r) => r.status < 500,
    '입찰 처리됨 (성공 또는 비즈니스 에러)': (r) => r.status < 500,
  });
}
