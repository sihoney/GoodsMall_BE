/**
 * 시나리오 A: 동시 입찰 경쟁 테스트 (Concurrent Bid Test)
 * 목적: 낙관락 환경에서 pending_overrun(헛 Payment 호출) 규모 측정
 *       HikariCP pool(max=5) 소진 시점 확인
 *
 * 낙관락 동작 원리:
 *   여러 VU가 동시에 경매 상세 조회 → 같은 currentHighestPrice 읽음
 *   → 동일 bidPrice로 동시 POST → 입찰 생성은 모두 PENDING 통과 (409 거의 없음)
 *   → Payment Kafka 응답 후 BidConfirmService가 @Version 충돌 시 재시도(MAX_RETRY=3)
 *   → 최종 ACTIVE는 1건, 나머지는 자동 취소 — pending_overrun이 핵심 지표
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
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { CONCURRENT_BID_AUCTION_ID, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { buyerHeaders, publicHeaders } from '../helpers/auth.js';
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

/**
 * 낙관락 정확성 검증 (테스트 종료 후 1회 실행)
 *
 * k6로 검증 가능한 범위:
 *   currentHighestPrice != null → BidConfirmService가 ACTIVE를 1건 이상 확정했다는 증거
 *
 * k6로 검증 불가한 범위 (DB 직접 확인 필요):
 *   ACTIVE가 정확히 1건인지, PENDING이 모두 소진됐는지는 전체 페이지를 봐야 알 수 있음.
 *   테스트 후 아래 SQL로 직접 확인:
 *   SELECT status, COUNT(*) FROM auction.bid
 *   WHERE auction_id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201' GROUP BY status;
 *   → ACTIVE=1, CANCELLED=N, PENDING=0 이면 낙관락 정상 동작
 */
export function teardown() {
  // Outbox 스케줄러(10s 주기) + Kafka 발행 + Payment 처리 + BidConfirmService retry 완료 대기
  const waitSeconds = parseInt(__ENV.TEARDOWN_WAIT || '20');
  console.log(`[teardown] ${waitSeconds}초 대기 — Kafka 비동기 처리 완료 대기 중...`);
  sleep(waitSeconds);

  const auctionRes = http.get(
    `${BASE_URL}/api/auctions/${CONCURRENT_BID_AUCTION_ID}`,
    { headers: publicHeaders() }
  );
  const auction = auctionRes.json('data');
  console.log(`[teardown] auction currentHighestPrice=${auction?.currentHighestPrice ?? null}`);
  console.log('[teardown] ACTIVE=1 여부는 DB 직접 확인: SELECT status, COUNT(*) FROM auction.bid WHERE auction_id=\'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201\' GROUP BY status;');

  check(auction, {
    '낙관락 보장: currentHighestPrice 확정됨 (ACTIVE bid 존재 증거)': (a) => a?.currentHighestPrice != null,
  });
}
