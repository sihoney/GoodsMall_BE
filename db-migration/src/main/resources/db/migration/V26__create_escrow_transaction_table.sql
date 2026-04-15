CREATE TABLE IF NOT EXISTS payment.escrow_transaction (
    escrow_transaction_id UUID         NOT NULL,
    escrow_id             UUID         NOT NULL,
    order_id              UUID         NOT NULL,
    order_item_id         UUID,
    seller_member_id      UUID         NOT NULL,
    buyer_member_id       UUID         NOT NULL,
    trasaction_type       VARCHAR(30)  NOT NULL,
    amount                BIGINT       NOT NULL,
    before_amount         BIGINT       NOT NULL,
    after_amount          BIGINT       NOT NULL,
    reference_id          UUID,
    reference_type        VARCHAR(30),
    description           VARCHAR(255),
    occurred_at           TIMESTAMP    NOT NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_escrow_transaction PRIMARY KEY (escrow_transaction_id),
    CONSTRAINT fk_escrow_transaction_escrow_id
        FOREIGN KEY (escrow_id) REFERENCES payment.escrow (escrow_id),
    CONSTRAINT chk_escrow_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_escrow_transaction_before_amount_non_negative CHECK (before_amount >= 0),
    CONSTRAINT chk_escrow_transaction_after_amount_non_negative CHECK (after_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_escrow_transaction_escrow_id
    ON payment.escrow_transaction (escrow_id);

CREATE INDEX IF NOT EXISTS idx_escrow_transaction_order_id
    ON payment.escrow_transaction (order_id);

CREATE INDEX IF NOT EXISTS idx_escrow_transaction_occurred_at
    ON payment.escrow_transaction (occurred_at);

CREATE INDEX IF NOT EXISTS idx_escrow_transaction_tx_type
    ON payment.escrow_transaction (tx_type);

