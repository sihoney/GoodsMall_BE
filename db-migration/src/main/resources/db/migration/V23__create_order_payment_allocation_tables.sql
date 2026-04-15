CREATE TABLE IF NOT EXISTS payment.order_payment (
    order_payment_id UUID         NOT NULL,
    order_id         UUID         NOT NULL,
    buyer_member_id  UUID         NOT NULL,
    total_amount     BIGINT       NOT NULL,
    payment_method   VARCHAR(20)  NOT NULL,
    payment_status   VARCHAR(30)  NOT NULL,
    paid_at          TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_order_payment PRIMARY KEY (order_payment_id),
    CONSTRAINT uq_order_payment_order_id UNIQUE (order_id),
    CONSTRAINT chk_order_payment_total_amount_positive CHECK (total_amount > 0),
    CONSTRAINT chk_order_payment_method
        CHECK (payment_method IN ('WALLET', 'CARD', 'MIXED')),
    CONSTRAINT chk_order_payment_status
        CHECK (payment_status IN ('REQUESTED', 'SUCCEEDED', 'FAILED', 'PARTIAL_REFUNDED', 'REFUNDED'))
);

CREATE INDEX IF NOT EXISTS idx_order_payment_buyer_member_id
    ON payment.order_payment (buyer_member_id);

CREATE INDEX IF NOT EXISTS idx_order_payment_status
    ON payment.order_payment (payment_status);

CREATE TABLE IF NOT EXISTS payment.order_payment_allocation (
    allocation_id               UUID         NOT NULL,
    order_payment_id            UUID         NOT NULL,
    method                      VARCHAR(20)  NOT NULL,
    amount                      BIGINT       NOT NULL,
    card_transaction_group_id   UUID,
    wallet_transaction_id       UUID,
    created_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_order_payment_allocation PRIMARY KEY (allocation_id),
    CONSTRAINT fk_order_payment_allocation_order_payment
        FOREIGN KEY (order_payment_id) REFERENCES payment.order_payment (order_payment_id),
    CONSTRAINT chk_order_payment_allocation_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_order_payment_allocation_method
        CHECK (method IN ('WALLET', 'CARD')),
    CONSTRAINT chk_order_payment_allocation_reference
        CHECK (
            (method = 'CARD' AND card_transaction_group_id IS NOT NULL AND wallet_transaction_id IS NULL)
                OR
            (method = 'WALLET' AND wallet_transaction_id IS NOT NULL AND card_transaction_group_id IS NULL)
            )
);

CREATE INDEX IF NOT EXISTS idx_order_payment_allocation_order_payment_id
    ON payment.order_payment_allocation (order_payment_id);

CREATE INDEX IF NOT EXISTS idx_order_payment_allocation_card_group
    ON payment.order_payment_allocation (card_transaction_group_id);

CREATE INDEX IF NOT EXISTS idx_order_payment_allocation_wallet_tx
    ON payment.order_payment_allocation (wallet_transaction_id);

CREATE TABLE IF NOT EXISTS payment.payment_refund_allocation (
    refund_allocation_id               UUID         NOT NULL,
    refund_id                          UUID         NOT NULL,
    method                             VARCHAR(20)  NOT NULL,
    amount                             BIGINT       NOT NULL,
    card_cancel_transaction_group_id   UUID,
    wallet_refund_transaction_id       UUID,
    created_at                         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payment_refund_allocation PRIMARY KEY (refund_allocation_id),
    CONSTRAINT fk_payment_refund_allocation_refund
        FOREIGN KEY (refund_id) REFERENCES payment.payment_refund (refund_id),
    CONSTRAINT chk_payment_refund_allocation_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_refund_allocation_method
        CHECK (method IN ('WALLET', 'CARD')),
    CONSTRAINT chk_payment_refund_allocation_reference
        CHECK (
            (method = 'CARD' AND card_cancel_transaction_group_id IS NOT NULL AND wallet_refund_transaction_id IS NULL)
                OR
            (method = 'WALLET' AND wallet_refund_transaction_id IS NOT NULL AND card_cancel_transaction_group_id IS NULL)
            )
);

CREATE INDEX IF NOT EXISTS idx_payment_refund_allocation_refund_id
    ON payment.payment_refund_allocation (refund_id);

CREATE INDEX IF NOT EXISTS idx_payment_refund_allocation_card_group
    ON payment.payment_refund_allocation (card_cancel_transaction_group_id);

CREATE INDEX IF NOT EXISTS idx_payment_refund_allocation_wallet_tx
    ON payment.payment_refund_allocation (wallet_refund_transaction_id);
