/**
 * 장기 안정성 테스트 (Soak Test)
 *
 * 목적:
 *   낮은 부하를 장시간 유지하면서 시간이 지날수록 나타나는 이상 징후를 탐지한다.
 *   - 메모리 누수: JVM Heap이 지속적으로 증가하는지 (Grafana 확인)
 *   - DB 커넥션 누수: HikariCP Active 커넥션이 점점 회복 불가 상태가 되는지
 *   - Kafka 이벤트 누적: Outbox 처리 지연으로 consumer lag이 증가하는지
 *   - 에러율 증가: 초반에는 없던 오류가 시간이 지나면서 나타나는지
 *
 * 주의:
 *   30분 통과는 "장애 없이 일정 부하 유지 가능" 의미일 뿐, 누수 없음 보장 아님.
 *   누수 패턴은 보통 2시간 이후에 드러남 → 진짜 누수 검증은 최소 2h, 권장 4~8h.
 *   Grafana에서 JVM heap / HikariCP active / Kafka consumer lag 동시 모니터링 필수.
 *
 * 실행:
 *   기본 (시연용 30분):  k6 run auction/k6/scenarios/soak.js
 *   누수 탐지 (4시간):   k6 run auction/k6/scenarios/soak.js -e DURATION=4h -e VUS=30
 * 전제: reset_test_auctions.sql 실행 후 시작
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { publicHeaders, buyerHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';
import { recordBidResult } from '../helpers/metrics.js';

const soakBidDuration = new Trend('soak_bid_duration', true);

const SOAK_DURATION = __ENV.DURATION || '30m';
const TARGET_VUS    = parseInt(__ENV.VUS || '20');

export const options = {
  stages: [
    { duration: '2m',           target: TARGET_VUS }, // ramp-up
    { duration: SOAK_DURATION,  target: TARGET_VUS }, // 장기 유지
    { duration: '2m',           target: 0 },           // ramp-down
  ],
  thresholds: {
    bid_server_error:    ['count<5'],
    soak_bid_duration:   ['p(99)<3000'],
    http_req_failed:     ['rate<0.02'],
    http_req_duration:   ['p(99)<3000'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand      = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.50) {
    // 50% 목록 조회
    const res = http.get(
      `${BASE_URL}/api/auctions?status=ONGOING&page=0&size=9`,
      { headers: publicHeaders() }
    );
    check(res, { 'soak list ok': (r) => r.status === 200 });
    sleep(1);

  } else if (rand < 0.80) {
    // 30% 상세 조회
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    check(res, { 'soak detail ok': (r) => r.status === 200 });
    sleep(0.5);

  } else {
    // 20% 입찰 (장시간 실행이라 잔액 소진 가능 — 필요 시 refill_wallet.sql 별도 실행)
    const bidderId = BASELINE_BIDDER_IDS[(__VU - 1) % BASELINE_BIDDER_IDS.length];
    const bidPrice = fetchCurrentBidPrice(auctionId);
    if (bidPrice !== null) {
      const res = http.post(
        `${BASE_URL}/api/auctions/${auctionId}/bids`,
        JSON.stringify({ bidPrice }),
        {
          headers: buyerHeaders(bidderId),
          responseCallback: http.expectedStatuses(201, 400, 409, 422),
        }
      );
      soakBidDuration.add(res.timings.duration);
      recordBidResult(res);
      check(res, { 'soak bid ok': (r) => r.status < 500 });
    }
    sleep(2);
  }
}
