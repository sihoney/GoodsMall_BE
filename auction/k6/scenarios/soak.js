/**
 * 시나리오 E: 장기 안정성 테스트 (Soak Test)
 * 목적: 메모리 누수, DB 커넥션 누수, Kafka 이벤트 누적 오류 확인
 *       JVM 힙 증가 추이, GC 빈도 관찰
 * 실행: k6 run auction/k6/scenarios/soak.js --out json=results/soak.json
 * 주의: 30~60분 소요 (Grafana에서 JVM 메모리 추이 반드시 모니터링)
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS } from '../helpers/data.js';
import { publicHeaders } from '../helpers/auth.js';

const totalErrors = new Counter('soak_total_errors');
const errorRate = new Rate('soak_error_rate');

const SOAK_DURATION = __ENV.DURATION || '30m';
const TARGET_VUS = parseInt(__ENV.VUS || '50');

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
    const isError = res.status >= 500;
    errorRate.add(isError ? 1 : 0);
    if (isError) totalErrors.add(1);
    check(res, { 'soak bid ok': (r) => r.status < 500 });
    sleep(2);
  }
}
