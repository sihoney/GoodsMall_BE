-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V4, V39
--
-- Explicitly excluded data-fix migrations:
-- V102
CREATE SCHEMA IF NOT EXISTS settlement;

CREATE TABLE IF NOT EXISTS settlement.settlement (
    settlement_id             UUID           NOT NULL,
    seller_id                 UUID           NOT NULL,
    settlement_year           INT            NOT NULL,
    settlement_month          INT            NOT NULL,
    total_sales_amount        DECIMAL(19,2)  NOT NULL DEFAULT 0,
    fee_amount                DECIMAL(19,2)  NOT NULL DEFAULT 0,
    final_settlement_amount   DECIMAL(19,2)  NOT NULL DEFAULT 0,
    settled_amount            DECIMAL(19,2)  NOT NULL DEFAULT 0,
    settlement_status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    settlement_type           VARCHAR(20)    NOT NULL DEFAULT 'MONTHLY',
    settled_at                TIMESTAMP,
    last_failure_reason       VARCHAR(500),
    requested_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id)
);

CREATE INDEX IF NOT EXISTS idx_settlement_seller_year_month_type
    ON settlement.settlement (seller_id, settlement_year, settlement_month, settlement_type);

CREATE UNIQUE INDEX IF NOT EXISTS uq_monthly_settlement_seller_year_month
    ON settlement.settlement (seller_id, settlement_year, settlement_month)
    WHERE settlement_type = 'MONTHLY';

CREATE TABLE IF NOT EXISTS settlement.settlement_item (
    settlement_item_id       UUID           NOT NULL,
    settlement_id            UUID,
    settlement_item_status   VARCHAR(30)    NOT NULL,
    order_id                 UUID           NOT NULL,
    escrow_id                UUID           NOT NULL,
    seller_id                UUID           NOT NULL,
    gross_amount             DECIMAL(19,2)  NOT NULL,
    fee_amount               DECIMAL(19,2)  NOT NULL,
    net_amount               DECIMAL(19,2)  NOT NULL,
    released_at              TIMESTAMP      NOT NULL,
    created_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_item PRIMARY KEY (settlement_item_id),
    CONSTRAINT uq_settlement_item_escrow_id UNIQUE (escrow_id)
);

CREATE TABLE IF NOT EXISTS settlement.settlement_refund_manual_action (
    manual_action_id    UUID           NOT NULL,
    event_id            UUID           NOT NULL,
    refund_id           UUID           NOT NULL,
    settlement_id       UUID           NOT NULL,
    settlement_item_id  UUID           NOT NULL,
    order_id            UUID           NOT NULL,
    escrow_id           UUID           NOT NULL,
    order_item_id       UUID           NOT NULL,
    seller_id           UUID           NOT NULL,
    buyer_id            UUID           NOT NULL,
    refund_amount       DECIMAL(19,2)  NOT NULL,
    reason              VARCHAR(100)   NOT NULL,
    occurred_at         TIMESTAMP      NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_refund_manual_action PRIMARY KEY (manual_action_id),
    CONSTRAINT uq_settlement_refund_manual_action_refund_escrow UNIQUE (refund_id, escrow_id),
    CONSTRAINT chk_settlement_refund_manual_action_amount_positive CHECK (refund_amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_settlement_refund_manual_action_settlement_id
    ON settlement.settlement_refund_manual_action (settlement_id);
CREATE INDEX IF NOT EXISTS idx_settlement_refund_manual_action_created_at
    ON settlement.settlement_refund_manual_action (created_at);

CREATE TABLE IF NOT EXISTS settlement.outbox_events (
    id                  UUID           PRIMARY KEY,
    topic               VARCHAR(200)   NOT NULL,
    event_type          VARCHAR(100)   NOT NULL,
    aggregate_id        VARCHAR(255)   NOT NULL,
    trace_id            VARCHAR(255),
    payload             TEXT           NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    retry_count         INT            NOT NULL DEFAULT 0,
    last_error_message  VARCHAR(1000),
    created_at          TIMESTAMP      NOT NULL,
    published_at        TIMESTAMP,
    CONSTRAINT chk_settlement_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_settlement_outbox_status_created_at
    ON settlement.outbox_events (status, created_at);
CREATE INDEX IF NOT EXISTS idx_settlement_outbox_aggregate_id
    ON settlement.outbox_events (aggregate_id);
