// 시드 데이터 기준 경매 ID
export const SEED_AUCTIONS = {
  // startPrice=50000, bidUnit=1000 / 테스트 실행 전 reset_test_auctions.sql로 current_highest_price 초기화 필요
  ONGOING: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',
  // 아래 두 ID는 현재 DB에 없음 — 필요 시 직접 INSERT 후 사용
  WAITING: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeee002',
  COMPLETED: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeee003',
};

// load_test_auctions.sql 로 삽입되는 부하테스트 전용 경매 ID 목록
// setup() 또는 SQL 시드 실행 후에 사용 가능
export const LOAD_TEST_AUCTION_IDS = [
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa101',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa102',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa103',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa104',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa105',
];

// 단일 경매 집중 테스트용 (Scenario A)
export const CONCURRENT_BID_AUCTION_ID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa201';

// 유효 입찰가 계산 (startPrice=50000, bidUnit=1000, currentHighest=null)
// currentHighestPrice가 없으면 startPrice 이상, 있으면 currentHighest + bidUnit 이상
export function validBidPrice(baseAmount = 50000) {
  return baseAmount;
}

/**
 * 부하테스트용 경매 목록에서 랜덤 선택
 */
export function randomAuctionId() {
  const ids = LOAD_TEST_AUCTION_IDS;
  return ids[Math.floor(Math.random() * ids.length)];
}

// 베이스라인 테스트 전용 고정 입찰자 풀 (wallet 보유 보장)
// 풀 크기 = 최대 VU 수(30)와 동일 → VU마다 전담 입찰자, 동일 입찰자 동시 사용 없음
// 사전 조건: seed_baseline_wallets.sql 최초 1회 실행
// 잔액 소진 시: refill_wallet.sql 실행
export const BASELINE_BIDDER_IDS = [
  '11111111-1111-1111-1111-111111111110',
  '11111111-1111-1111-1111-111111111111',
  '11111111-1111-1111-1111-111111111112',
  '11111111-1111-1111-1111-111111111113',
  '11111111-1111-1111-1111-111111111114',
  '11111111-1111-1111-1111-111111111115',
  '11111111-1111-1111-1111-111111111116',
  '11111111-1111-1111-1111-111111111117',
  '11111111-1111-1111-1111-111111111118',
  '11111111-1111-1111-1111-111111111119',
  '11111111-1111-1111-1111-111111111120',
  '11111111-1111-1111-1111-111111111121',
  '11111111-1111-1111-1111-111111111122',
  '11111111-1111-1111-1111-111111111123',
  '11111111-1111-1111-1111-111111111124',
  '11111111-1111-1111-1111-111111111125',
  '11111111-1111-1111-1111-111111111126',
  '11111111-1111-1111-1111-111111111127',
  '11111111-1111-1111-1111-111111111128',
  '11111111-1111-1111-1111-111111111129',
  '11111111-1111-1111-1111-111111111130',
  '11111111-1111-1111-1111-111111111131',
  '11111111-1111-1111-1111-111111111132',
  '11111111-1111-1111-1111-111111111133',
  '11111111-1111-1111-1111-111111111134',
  '11111111-1111-1111-1111-111111111135',
  '11111111-1111-1111-1111-111111111136',
  '11111111-1111-1111-1111-111111111137',
  '11111111-1111-1111-1111-111111111138',
  '11111111-1111-1111-1111-111111111139',
];
