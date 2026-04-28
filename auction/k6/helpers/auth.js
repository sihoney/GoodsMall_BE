import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// 판매자 UUID (시드 데이터 기준)
export const SELLER_ID = '22222222-2222-2222-2222-222222222202';

// 기본 입찰자 (시드 데이터)
export const DEFAULT_BIDDER_ID = '11111111-1111-1111-1111-111111111101';

/**
 * VU별 고유 입찰자 헤더 반환.
 * - 게이트웨이 없이 경매 서비스(8090)에 직접 요청할 때 사용
 * - BUYER 역할로 설정 (판매자는 자신의 경매에 입찰 불가)
 */
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
