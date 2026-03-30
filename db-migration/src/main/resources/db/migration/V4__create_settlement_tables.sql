CREATE TABLE IF NOT EXISTS settlement.settlement (
    settlement_id            UUID         NOT NULL,
    seller_id                UUID         NOT NULL,
    settlement_year          INT          NOT NULL,
    settlement_month         INT          NOT NULL,
    total_sales_amount       BIGINT       NOT NULL DEFAULT 0,
    fee_amount               BIGINT       NOT NULL DEFAULT 0,
    final_settlement_amount  BIGINT       NOT NULL DEFAULT 0,
    settled_amount           BIGINT       NOT NULL DEFAULT 0,
    settlement_status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    settled_at               TIMESTAMP,
    last_failure_reason      VARCHAR(500),
    requested_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id)
);

CREATE TABLE IF NOT EXISTS settlement.settlement_item (
    settlement_item_id UUID      NOT NULL,
    settlement_id      UUID,
    order_id           UUID      NOT NULL,
    escrow_id          UUID      NOT NULL,
    seller_id          UUID      NOT NULL,
    gross_amount       BIGINT    NOT NULL,
    fee_amount         BIGINT    NOT NULL,
    net_amount         BIGINT    NOT NULL,
    released_at        TIMESTAMP NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_item PRIMARY KEY (settlement_item_id),
    CONSTRAINT uq_settlement_item_escrow_id UNIQUE (escrow_id)
);
