/**
 * 스파이크 테스트 (Spike Test)
 *
 * 목적:
 *   경매 마감 직전 폭발적 트래픽(입찰 러시)을 재현한다.
 *   평상시 10 VU → 갑자기 100 VU (30초) → 다시 10 VU 로 전환 시
 *   - 스파이크 구간에서 서버 오류(5xx)가 허용 범위 내인지 확인
 *   - 스파이크 해소 후 응답시간이 정상으로 회복되는지 확인
 *
 * 실행: k6 run auction/k6/scenarios/spike.js --out json=results/spike.json
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

const spikeBidDuration = new Trend('spike_bid_duration', true);

export const options = {
  stages: [
    { duration: '2m',  target: 10  }, // 평상시 트래픽
    { duration: '10s', target: 100 }, // 스파이크 시작 (경매 마감 러시)
    { duration: '30s', target: 100 }, // 스파이크 지속
    { duration: '10s', target: 10  }, // 스파이크 해소
    { duration: '2m',  target: 10  }, // 회복 관찰
    { duration: '1m',  target: 0   }, // 종료
  ],
  thresholds: {
    bid_server_error:     ['count<10'],
    spike_bid_duration:   ['p(99)<3000'],
    http_req_failed:      ['rate<0.10'],
  },
};

const AUCTION_IDS = [SEED_AUCTIONS.ONGOING, ...LOAD_TEST_AUCTION_IDS];

export default function () {
  const rand      = Math.random();
  const auctionId = AUCTION_IDS[Math.floor(Math.random() * AUCTION_IDS.length)];

  if (rand < 0.30) {
    // 30% 입찰
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
      spikeBidDuration.add(res.timings.duration);
      recordBidResult(res);
      check(res, { 'spike 입찰 처리됨': (r) => r.status < 500 });
    }

  } else {
    // 70% 조회
    const res = http.get(
      `${BASE_URL}/api/auctions/${auctionId}`,
      { headers: publicHeaders() }
    );
    check(res, { 'spike 조회 200': (r) => r.status === 200 });
    sleep(0.5);
  }
}
