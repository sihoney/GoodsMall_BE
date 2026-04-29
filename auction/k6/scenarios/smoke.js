/**
 * 스모크 테스트 (Smoke Test)
 * 목적: 기본 기능 정상 동작 확인 (부하 테스트 전 사전 검증)
 * 실행: k6 run auction/k6/scenarios/smoke.js
 */
/**
 * 스모크 테스트 (Smoke Test)
 * 목적: 기본 기능 정상 동작 확인 (부하 테스트 전 사전 검증)
 * 실행: k6 run auction/k6/scenarios/smoke.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from '../config/thresholds.js';
import { login, authenticatedHeaders, publicHeaders, BUYER_EMAIL, BUYER_PASSWORD } from '../helpers/auth.js';
import { SEED_AUCTIONS } from '../helpers/data.js';

export const options = {
  vus: 2,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(99)<2000'],
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  const buyerToken = login(BUYER_EMAIL, BUYER_PASSWORD);
  return { buyerToken };
}

export default function (data) {
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

  // 2. 단일 경매 조회
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
  check(bidsRes, {
    '입찰 목록 200': (r) => r.status === 200,
  });

  // 4. 입찰 시도 (JWT 인증)
  const bidRes = http.post(
    `${BASE_URL}/api/auctions/${SEED_AUCTIONS.ONGOING}/bids`,
    JSON.stringify({ bidPrice: 55000 }),
    { headers: authenticatedHeaders(data.buyerToken) }
  );
  if (bidRes.status >= 500) {
    console.log(`[입찰 오류] status=${bidRes.status} body=${bidRes.body}`);
  }
  check(bidRes, {
    // 201(성공) 또는 400/409/422(비즈니스 에러)만 허용, 5xx는 안됨
    '입찰 5xx 없음': (r) => r.status < 500,
    '입찰 요청 처리됨': (r) => [201, 400, 409, 422].includes(r.status),
  });

  sleep(1);
}
