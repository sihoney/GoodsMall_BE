-- bid.status에 PENDING 추가 (예치금 차감 대기 상태)
-- + 기본값을 PENDING으로 설정 (입찰 생성 시 예치금 확정 전 상태로 시작)

ALTER TABLE auction.bid DROP CONSTRAINT chk_bid_status;

ALTER TABLE auction.bid ADD CONSTRAINT chk_bid_status
    CHECK (status IN (
        'PENDING',
        'ACTIVE',
        'OUTBID',
        'WINNING',
        'CANCELED',
        'PAYMENT_COMPLETED'
    ));

ALTER TABLE auction.bid ALTER COLUMN status SET DEFAULT 'PENDING';
