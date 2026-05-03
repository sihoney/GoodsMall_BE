/**
 * 시나리오 E: 장기 안정성 테스트 (Soak Test)
 * 목적: 메모리 누수, DB 커넥션 누수, Kafka 이벤트 누적 오류 확인
 *       JVM 힙 증가 추이, GC 빈도 관찰
 *
 * 실행:
 *   기본 (시연용 30분):  k6 run auction/k6/scenarios/soak.js --out json=results/soak.json
 *   누수 탐지 (4시간):   k6 run auction/k6/scenarios/soak.js -e DURATION=4h -e VUS=30
 *
 * 주의:
 *   - 30분 통과는 "장애 없이 일정 부하 유지 가능" 의미일 뿐, 누수 없음 보장 아님
 *   - 누수 패턴은 보통 2시간 이후에 드러남 → 진짜 누수 검증은 최소 2h, 권장 4~8h
 *   - Grafana 에서 JVM heap / HikariCP active / Kafka consumer lag 동시 모니터링 필수
 * 전제: reset_test_auctions.sql 실행 후 시작
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS, BASELINE_BIDDER_IDS } from '../helpers/data.js';
import { publicHeaders, buyerHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const totalErrors = new Counter('soak_total_errors');
const errorRate = new Rate('soak_error_rate');

const SOAK_DURATION = __ENV.DURATION || '30m';
const TARGET_VUS = parseInt(__ENV.VUS || '20');

export const options = {
  stages: [
    { duration: '2m', target: TARGET_VUS }, // ramp-up
    { duration: SOAK_DURATION, target: TARGET_VUS }, // 장기 유지
    { duration: '2m', target: 0 },           // ramp-down
  ],
  thresholds: {
    soak_error_rate: ['rate<0.02'],
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(99)<3000'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.50) {
    const res = http.get(
      `${BASE_URL}/api/auctions?status=ONGOING&page=0&size=9`,
      { headers: publicHeaders() }
    );
    const isError = res.status >= 500;
    errorRate.add(isError ? 1 : 0);
    if (isError) totalErrors.add(1);
    check(res, { 'soak list ok': (r) => r.status === 200 });
    sleep(1);

  } else if (rand < 0.80) {
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    const isError = res.status >= 500;
    errorRate.add(isError ? 1 : 0);
    if (isError) totalErrors.add(1);
    check(res, { 'soak detail ok': (r) => r.status === 200 });
    sleep(0.5);

  } else {
    // 20% 입찰 (wallet 보유 시드 입찰자 풀에서 VU별 배정)
    // 장시간 실행이라 잔액 소진 가능 — 필요 시 refill_wallet.sql 별도 실행
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
      const isError = res.status >= 500;
      errorRate.add(isError ? 1 : 0);
      if (isError) totalErrors.add(1);
      check(res, { 'soak bid ok': (r) => r.status < 500 });
    }
    sleep(2);
  }
}
