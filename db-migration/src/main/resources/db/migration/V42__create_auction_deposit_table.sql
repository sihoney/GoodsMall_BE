CREATE TABLE payment.auction_deposit (
    auction_deposit_id UUID PRIMARY KEY,
    auction_id UUID NOT NULL,
    bid_id UUID NOT NULL UNIQUE,
    bidder_id UUID NOT NULL,
    deposit_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    hold_wallet_transaction_id UUID NOT NULL,
    refund_wallet_transaction_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_auction_deposit_auction_id
    ON payment.auction_deposit (auction_id);

CREATE INDEX idx_auction_deposit_auction_id_status
    ON payment.auction_deposit (auction_id, status);
