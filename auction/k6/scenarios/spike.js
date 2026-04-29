/**
 * 시나리오 C: 스파이크 테스트 (Spike Test)
 * 목적: 경매 마감 직전 폭발적 트래픽(입찰 러시) 재현
 *       평상시 10 VU → 갑자기 100 VU (30초) → 다시 10 VU
 *       스파이크 시 에러율, 응답시간 저하, 회복 시간 측정
 * 실행: k6 run auction/k6/scenarios/spike.js --out json=results/spike.json
 * 전제: reset_test_auctions.sql 실행 후 시작
 *
 * VU 스케일 근거 (HikariCP max=5):
 *   100 VU 스파이크면 평상시 대비 10배 — 토이 서비스 기준 충분한 임팩트
 *   스파이크 효과는 VU 수 자체로 결정, 모든 VU 동일 패턴 사용
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { BASE_URL } from '../config/thresholds.js';
import { SEED_AUCTIONS, LOAD_TEST_AUCTION_IDS } from '../helpers/data.js';
import { publicHeaders } from '../helpers/auth.js';
import { fetchCurrentBidPrice } from '../helpers/bid.js';

const spikeBidDuration = new Trend('spike_bid_duration', true);
const spikeErrorRate = new Rate('spike_error_rate');
const bidsDuringSpike = new Counter('bids_during_spike');

export const options = {
  stages: [
    { duration: '2m', target: 10 },   // 평상시 트래픽
    { duration: '10s', target: 100 }, // 스파이크 시작 (경매 마감 러시)
    { duration: '30s', target: 100 }, // 스파이크 지속
    { duration: '10s', target: 10 },  // 스파이크 해소
    { duration: '2m', target: 10 },   // 회복 관찰
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    spike_error_rate: ['rate<0.10'],
    http_req_failed: ['rate<0.10'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.30) {
    // 30% 입찰 (스파이크 효과는 VU 수 증가로 자연스럽게 반영됨)
    const bidPrice = fetchCurrentBidPrice(auctionId);
    if (bidPrice !== null) {
      const res = http.post(
        `${BASE_URL}/api/auctions/${auctionId}/bids`,
        JSON.stringify({ bidPrice }),
        {
          headers: {
            'Content-Type': 'application/json',
            'X-Member-Id': uuidv4(),
            'X-Member-Role': 'USER',
            'X-Session-Id': uuidv4(),
          },
        }
      );
      spikeBidDuration.add(res.timings.duration);
      spikeErrorRate.add(res.status >= 500 ? 1 : 0);
      bidsDuringSpike.add(1);
      check(res, { 'spike 입찰 처리됨': (r) => r.status < 500 });
    }

  } else {
    // 70% 조회
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    spikeErrorRate.add(res.status >= 500 ? 1 : 0);
    check(res, { 'spike 조회 200': (r) => r.status === 200 });
    sleep(0.5);
  }
}
