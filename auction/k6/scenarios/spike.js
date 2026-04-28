/**
 * 시나리오 C: 스파이크 테스트 (Spike Test)
 * 목적: 경매 마감 직전 폭발적 트래픽(입찰 러시) 재현
 *       - 평상시 20 VU → 갑자기 500 VU (30초) → 다시 20 VU
 *       - 스파이크 시 에러율, 응답시간 저하, 회복 시간 측정
 * 실행: k6 run auction/k6/scenarios/spike.js --out json=results/spike.json
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS } from '../helpers/data.js';
import { publicHeaders } from '../helpers/auth.js';

const spikeBidDuration = new Trend('spike_bid_duration', true);
const spikeErrorRate = new Rate('spike_error_rate');
const bidsDuringSpike = new Counter('bids_during_spike');

export const options = {
  stages: [
    { duration: '2m', target: 20 },   // 평상시 트래픽
    { duration: '10s', target: 500 }, // 스파이크 시작 (경매 마감 러시)
    { duration: '30s', target: 500 }, // 스파이크 지속
    { duration: '10s', target: 20 },  // 스파이크 해소
    { duration: '3m', target: 20 },   // 회복 관찰
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    spike_error_rate: ['rate<0.10'],  // 스파이크 중 에러율 10% 미만
    http_req_failed: ['rate<0.10'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const isSpike = __VU > 20; // 스파이크 구간의 추가 VU 여부
  const rand = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.30 && isSpike) {
    // 스파이크 구간: 주로 입찰
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
    spikeBidDuration.add(res.timings.duration);
    spikeErrorRate.add(res.status >= 500 ? 1 : 0);
    bidsDuringSpike.add(1);
    check(res, { 'spike 입찰 처리됨': (r) => r.status < 500 });

  } else {
    // 평상시: 조회 위주
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    spikeErrorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'spike 조회 200': (r) => r.status === 200 });
    sleep(0.5);
  }
}
