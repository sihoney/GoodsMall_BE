import http from 'k6/http';
import { BASE_URL } from '../config/thresholds.js';

/**
 * 경매 상세 조회 후 유효한 최소 입찰가 반환.
 * 여러 VU가 동시에 호출하면 같은 currentHighestPrice를 읽어 동시 입찰 경합이 발생한다.
 * 조회 실패 시 null 반환.
 */
export function fetchCurrentBidPrice(auctionId) {
  const res = http.get(`${BASE_URL}/api/auctions/${auctionId}`, {
    headers: { 'Content-Type': 'application/json' },
  });
  if (res.status !== 200) return null;
  const data = res.json('data');
  if (!data) return null;
  const current = data.currentHighestPrice ?? 0;
  return Math.max(data.startPrice, current + data.bidUnit);
}
