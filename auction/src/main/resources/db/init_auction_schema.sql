CREATE TABLE auction
(
    auction_id            UUID PRIMARY KEY,
    product_id            UUID           NOT NULL,
    seller_id             UUID           NOT NULL,
    start_price           DECIMAL(19, 2) NOT NULL,
    current_highest_price DECIMAL(19, 2),
    duration_minutes      INTEGER        NOT NULL,
    started_at            TIMESTAMP      NOT NULL,
    ended_at              TIMESTAMP,
    status                VARCHAR(30)    NOT NULL DEFAULT 'WAITING',
    created_at            TIMESTAMP      NOT NULL,
    updated_at            TIMESTAMP      NOT NULL,

    CONSTRAINT chk_auction_status
        CHECK (status IN (
            'WAITING',
            'ONGOING',
            'PENDING_PAYMENT',
            'COMPLETED',
            'FAILED'
        ))
);

CREATE INDEX idx_auction_product_id ON auction (product_id);
CREATE INDEX idx_auction_seller_id  ON auction (seller_id);
CREATE INDEX idx_auction_status     ON auction (status);
CREATE INDEX idx_auction_started_at ON auction (started_at);

-- Bid
CREATE TABLE bid
(
    bid_id     UUID PRIMARY KEY,
    auction_id UUID           NOT NULL,
    bidder_id  UUID           NOT NULL,
    bid_price  DECIMAL(19, 2) NOT NULL,
    status     VARCHAR(30)    NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,

    CONSTRAINT fk_bid_auction
        FOREIGN KEY (auction_id) REFERENCES auction (auction_id),

    CONSTRAINT chk_bid_status
        CHECK (status IN (
            'ACTIVE',
            'OUTBID',
            'WINNING',
            'CANCELED',
            'PAYMENT_COMPLETED'
        ))
);

CREATE INDEX idx_bid_auction_id         ON bid (auction_id);
CREATE INDEX idx_bid_auction_price_desc ON bid (auction_id, bid_price DESC);
CREATE INDEX idx_bid_bidder_id          ON bid (bidder_id);
CREATE INDEX idx_bid_status             ON bid (auction_id, status);
