/**
 * 스모크 테스트 (Smoke Test)
 * 목적: 기본 기능 정상 동작 확인 (부하 테스트 전 사전 검증)
 * 실행: k6 run auction/k6/scenarios/smoke.js
 * 전제: reset_test_auctions.sql 실행 후 시작
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../config/thresholds.js';
import { buyerHeaders, publicHeaders } from '../helpers/auth.js';
import { SEED_AUCTIONS } from '../helpers/data.js';

export const options = {
  vus: 2,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(99)<2000'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  // 1. 경매 목록 조회
  const listRes = http.get(
    `${BASE_URL}/api/auctions?status=ONGOING&page=0&size=9`,
    { headers: publicHeaders() }
  );
  check(listRes, {
    '경매 목록 200': (r) => r.status === 200,
    '경매 목록 응답 구조': (r) => {
      const body = JSON.parse(r.body);
      return body.data !== undefined;
    },
  });

  // 2. 단일 경매 조회 + 최소 입찰가 계산
  const detailRes = http.get(
    `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}`,
    { headers: publicHeaders() }
  );
  check(detailRes, {
    '경매 상세 200': (r) => r.status === 200,
    '경매 상태 ONGOING': (r) => {
      const body = JSON.parse(r.body);
      return body.data && body.data.status === 'ONGOING';
    },
  });

  // 3. 입찰 목록 조회
  const bidsRes = http.get(
    `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}/bids`,
    { headers: publicHeaders() }
  );
  check(bidsRes, { '입찰 목록 200': (r) => r.status === 200 });

  // 4. 입찰 (상세 조회 결과로 최소 입찰가 동적 계산)
  //
  // bidder는 매 iteration마다 신규 UUID 사용 (buyerHeaders 인자 생략 시 uuidv4()).
  // 고정 ID 를 쓰면 첫 입찰이 ACTIVE 확정된 뒤부터 같은 사람이 또 입찰을 시도하게 되어
  // HIGHEST_BIDDER_CANNOT_REBID(400) 가 반복 발생 → "기능 정상 동작 확인"이라는
  // 스모크 테스트 본래 목적이 사실상 검증 안 되므로, 매번 다른 입찰자로 가정한다.
  //
  // 201 외 4xx 가 일부 나올 수 있는 정상 케이스:
  //   - 동시 입찰(VU=2)로 늦게 도착한 쪽이 BidIncrementNotMet(400)
  //   - GET 시점과 POST 시점 사이에 다른 VU가 끼어들어 stale 한 값으로 BidIncrement 미달(400)
  //   - 누적 차감으로 wallet 잔액 부족 시 INSUFFICIENT_DEPOSIT(409)
  // → 5xx만 실패로 카운트하며, 위 4xx는 도메인 룰이 정상 작동했다는 신호로 간주.
  let bidPrice = null;
  if (detailRes.status === 200) {
    const d = detailRes.json('data');
    if (d) {
      const current = d.currentHighestPrice ?? 0;
      bidPrice = Math.max(d.startPrice, current + d.bidUnit);
    }
  }

  if (bidPrice !== null) {
    const bidRes = http.post(
      `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}/bids`,
      JSON.stringify({ bidPrice }),
      {
        headers: buyerHeaders(),
        responseCallback: http.expectedStatuses(201, 400, 409, 422),
      }
    );
    if (bidRes.status >= 500) {
      console.log('BID ERROR status=' + bidRes.status + ' body=' + bidRes.body);
    }
    check(bidRes, {
      '입찰 5xx 없음': (r) => r.status < 500,
      '입찰 요청 처리됨': (r) => [201, 400, 409, 422].includes(r.status),
    });
  }

  sleep(1);
}
