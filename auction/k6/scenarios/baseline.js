/**
 * 기준선 측정 (Baseline Test)
 *
 * 목적:
 *   부하 테스트 전 정상 트래픽 구간에서의 응답시간 기준선을 측정한다.
 *   이후 load/stress/spike 테스트 결과와 비교할 기준(p50/p90/p99)을 확보한다.
 *
 * 실행: k6 run auction/k6/scenarios/baseline.js --out json=results/baseline.json
 * 전제:
 *   1) seed_baseline_wallets.sql 최초 1회 실행 (입찰자 wallet 생성)
 *   2) reset_test_auctions.sql 실행 후 시작
 *
 * 입찰자 전략:
 *   - BASELINE_BIDDER_IDS 풀(30명)에서 (__VU-1) % 30 으로 배정
 *   - VU마다 전담 입찰자를 가지므로 동일 입찰자 동시 사용 없음
 *   - 400/409/422는 정상 비즈니스 케이스로 허용 (expectedStatuses로 명시)
 *   - 실제 wallet 예치금 hold → Kafka 이벤트 → 결제 서비스 전체 흐름 측정
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { buyerHeaders, publicHeaders } from '../helpers/auth.js';
import { SEED_AUCTIONS, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';
import { recordBidResult } from '../helpers/metrics.js';

const bidDuration    = new Trend('bid_duration', true);
const listDuration   = new Trend('list_duration', true);
const detailDuration = new Trend('detail_duration', true);

export const options = {
  stages: [
    { duration: '1m', target: 10 }, // warm-up
    { duration: '3m', target: 30 }, // 기준 부하
    { duration: '1m', target: 0 },  // cool-down
  ],
  thresholds: {
    bid_duration:    ['p(90)<500', 'p(99)<2000'],
    list_duration:   ['p(90)<300', 'p(99)<1000'],
    detail_duration: ['p(90)<200', 'p(99)<500'],
    bid_server_error: ['count==0'],
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
    // 20% 입찰 시도 (wallet 예치금 hold → Kafka → 결제 서비스 전체 흐름)
    const bidderId = BASELINE_BIDDER_IDS[(__VU - 1) % BASELINE_BIDDER_IDS.length];
    const bidPrice = fetchCurrentBidPrice(SEED_AUCTIONS.ONGOING);
    if (bidPrice !== null) {
      const res = http.post(
        `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}/bids`,
        JSON.stringify({ bidPrice }),
        {
          headers: buyerHeaders(bidderId),
          responseCallback: http.expectedStatuses(201, 400, 409, 422),
        }
      );
      bidDuration.add(res.timings.duration);
      recordBidResult(res);
      check(res, { '입찰 5xx 없음': (r) => r.status < 500 });
    }
  }

  sleep(0.5);
}
