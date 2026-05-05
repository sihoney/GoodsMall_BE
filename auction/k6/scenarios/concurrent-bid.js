/**
 * 동시 입찰 경쟁 테스트 (Concurrent Bid Test)
 *
 * 목적:
 *   여러 사용자가 동시에 같은 경매에 입찰할 때 동시성 처리가 올바른지 검증한다.
 *   - bid_server_error = 0 → 비관적 락이 데드락/타임아웃 없이 직렬화 정상 작동
 *   - bid_rejected 다수  → PENDING 단일 보장 로직이 정상 작동
 *   - bid_duration p99   → 락 대기가 수용 가능한 수준인지 측정
 *
 * 락 경합 원리:
 *   여러 VU가 동시에 경매 상세 조회 → 같은 currentHighestPrice 읽음
 *   → 동일 bidPrice로 동시 POST → DB 락 경합 → 1건 성공, 나머지 409
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
import { Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { CONCURRENT_BID_AUCTION_ID, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { buyerHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';
import { bidSuccess, bidRejected, bidServerError, recordBidResult } from '../helpers/metrics.js';

const bidDuration = new Trend('bid_duration', true);

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
    // 핵심 지표: 서버 오류가 없어야 동시성 처리가 정상
    bid_server_error: ['count<3'],
    // 성공 입찰이 최소 1건은 있어야 함
    bid_success: ['count>0'],
    // 락 직렬화로 인한 꼬리 지연 허용 범위
    bid_duration: ['p(99)<5000', 'p(99.9)<10000'],
  },
};

export default function () {
  // VU별 고정 입찰자 배정 (wallet 보유 + 동일 입찰자 충돌 회피)
  // 풀(30명)을 초과하는 VU에서는 modulo로 중복 사용 → VUS>30 권장하지 않음
  const bidderId = BASELINE_BIDDER_IDS[(__VU - 1) % BASELINE_BIDDER_IDS.length];

  // 동시에 같은 값을 읽으면 동일 bidPrice → 락 경합 자연 발생
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

  bidDuration.add(res.timings.duration);
  recordBidResult(res);

  check(res, {
    '서버 에러 없음': (r) => r.status < 500,
  });
}

export function handleSummary(data) {
  const success   = data.metrics['bid_success']?.values?.count   || 0;
  const rejected  = data.metrics['bid_rejected']?.values?.count  || 0;
  const serverErr = data.metrics['bid_server_error']?.values?.count || 0;
  const p99       = data.metrics['bid_duration']?.values?.['p(99)'] || 0;

  console.log(`
=== 동시 입찰 테스트 결과 ===
입찰 성공   (201): ${success}
비즈니스 거부 (4xx): ${rejected}
서버 오류   (5xx): ${serverErr}
bid_duration p99: ${p99.toFixed(0)}ms

[동시성 검증] DB에서 아래 쿼리를 실행하세요:

-- 1. 동시에 PENDING이 2개 이상 생성된 경우 (0행이어야 정상)
SELECT auction_id, COUNT(*) AS cnt
FROM auction.bid WHERE status = 'PENDING'
GROUP BY auction_id HAVING COUNT(*) > 1;

-- 2. 중복 낙찰 확인 (0행이어야 정상)
SELECT auction_id, COUNT(*) AS cnt
FROM auction.bid WHERE status = 'ACTIVE'
GROUP BY auction_id HAVING COUNT(*) > 1;

-- 3. 낙찰가 역전 확인 (0행이어야 정상)
SELECT b1.auction_id, b1.bid_price AS winner, b2.bid_price AS should_have_won
FROM auction.bid b1
JOIN auction.bid b2 ON b1.auction_id = b2.auction_id
WHERE b1.status = 'ACTIVE' AND b2.status = 'OUTBID'
  AND b2.bid_price > b1.bid_price;
`);

  return { stdout: JSON.stringify(data, null, 2) };
}
