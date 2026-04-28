// 시드 데이터 기준 경매 ID
export const SEED_AUCTIONS = {
  ONGOING: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeee001',  // startPrice=50000, bidUnit=1000, currentHighest=52000
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
