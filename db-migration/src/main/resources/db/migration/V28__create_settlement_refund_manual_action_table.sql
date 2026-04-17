CREATE TABLE IF NOT EXISTS settlement.settlement_refund_manual_action (
    manual_action_id   UUID         NOT NULL,
    event_id           UUID         NOT NULL,
    refund_id          UUID         NOT NULL,
    settlement_id      UUID         NOT NULL,
    settlement_item_id UUID         NOT NULL,
    order_id           UUID         NOT NULL,
    escrow_id          UUID         NOT NULL,
    order_item_id      UUID         NOT NULL,
    seller_id          UUID         NOT NULL,
    buyer_id           UUID         NOT NULL,
    refund_amount      BIGINT       NOT NULL,
    reason             VARCHAR(100) NOT NULL,
    occurred_at        TIMESTAMP    NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_refund_manual_action PRIMARY KEY (manual_action_id),
    CONSTRAINT uq_settlement_refund_manual_action_refund_escrow UNIQUE (refund_id, escrow_id),
    CONSTRAINT chk_settlement_refund_manual_action_amount_positive CHECK (refund_amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_settlement_refund_manual_action_settlement_id
    ON settlement.settlement_refund_manual_action (settlement_id);

CREATE INDEX IF NOT EXISTS idx_settlement_refund_manual_action_created_at
    ON settlement.settlement_refund_manual_action (created_at);
