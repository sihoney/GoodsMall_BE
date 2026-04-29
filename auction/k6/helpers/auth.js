import http from 'k6/http';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { MEMBER_URL } from '../config/thresholds.js';

// 판매자 UUID (시드 데이터 기준)
export const SELLER_ID = '22222222-2222-2222-2222-222222222202';

// 기본 입찰자 (시드 데이터)
// 현재 미사용 — 모든 시나리오는 매 iteration마다 uuidv4() 로 신규 입찰자를 만들어
// HIGHEST_BIDDER_CANNOT_REBID 룰을 자연스럽게 우회한다.
// 시드 계정으로 직접 입찰하는 시나리오를 새로 추가할 때 import 하여 사용.
export const DEFAULT_BIDDER_ID = '11111111-1111-1111-1111-111111111101';

// 시드 계정 정보
export const BUYER_EMAIL = 'buyer@test.local';
export const BUYER_PASSWORD = '1111';
export const SELLER_EMAIL = 'seller@test.local';
export const SELLER_PASSWORD = '2222';

/**
 * member 서비스에 로그인하여 JWT accessToken 반환.
 * setup() 단계에서 호출하여 VU에 전달한다.
 */
export function login(email, password) {
  const res = http.post(
    `${MEMBER_URL}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const token = res.json('data.accessToken');
  if (!token) {
    throw new Error(`로그인 실패 [${res.status}]: ${res.body}`);
  }
  return token;
}

/**
 * JWT 토큰을 Authorization 헤더에 담아 반환.
 * 인증이 필요한 요청(입찰 등)에 사용.
 */
export function authenticatedHeaders(token) {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

// 아래 함수들은 다른 시나리오(baseline, load 등)와의 호환성을 위해 유지
export function buyerHeaders(memberId) {
  const id = memberId || uuidv4();
  return {
    'Content-Type': 'application/json',
    'X-Member-Id': id,
    'X-Member-Role': 'BUYER',
    'X-Session-Id': uuidv4(),
  };
}

export function sellerHeaders() {
  return {
    'Content-Type': 'application/json',
    'X-Member-Id': SELLER_ID,
    'X-Member-Role': 'SELLER',
    'X-Session-Id': uuidv4(),
  };
}

export function publicHeaders() {
  return { 'Content-Type': 'application/json' };
}
