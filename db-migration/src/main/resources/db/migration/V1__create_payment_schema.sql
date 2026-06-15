-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V1, V2, V3, V15, V18, V19, V20, V21, V22, V25, V26, V27, V28,
-- V34, V35, V37, V38, V40, V41, V46, V50, V51, V105
--
-- Explicitly excluded data-fix migrations:
-- V16
CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE IF NOT EXISTS payment.wallet (
    wallet_id   UUID           NOT NULL,
    member_id   UUID           NOT NULL,
    balance     DECIMAL(19,2)  NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet PRIMARY KEY (wallet_id),
    CONSTRAINT uq_wallet_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS payment.charge (
    charge_id         UUID           NOT NULL,
    member_id         UUID           NOT NULL,
    wallet_id         UUID,
    requested_amount  DECIMAL(19,2)  NOT NULL,
    approved_amount   DECIMAL(19,2),
    toss_bank_code    VARCHAR(30),
    pg_order_id       VARCHAR(100)   NOT NULL,
    pg_payment_key    VARCHAR(200),
    charge_status     VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    requested_at      TIMESTAMP      NOT NULL,
    approved_at       TIMESTAMP,
    failed_at         TIMESTAMP,
    failure_reason    VARCHAR(500),
    CONSTRAINT pk_charge PRIMARY KEY (charge_id),
    CONSTRAINT uq_charge_pg_order_id UNIQUE (pg_order_id)
);

CREATE TABLE IF NOT EXISTS payment.charge_refund (
    charge_refund_id  UUID           NOT NULL,
    charge_id         UUID           NOT NULL,
    refund_amount     DECIMAL(19,2)  NOT NULL,
    refund_reason     VARCHAR(255)   NOT NULL,
    refund_status     VARCHAR(30)    NOT NULL,
    requested_at      TIMESTAMP      NOT NULL,
    refunded_at       TIMESTAMP,
    failed_at         TIMESTAMP,
    failure_reason    VARCHAR(500),
    CONSTRAINT pk_charge_refund PRIMARY KEY (charge_refund_id)
);

CREATE TABLE IF NOT EXISTS payment.wallet_transaction (
    transaction_id    UUID           NOT NULL,
    wallet_id         UUID           NOT NULL,
    amount            DECIMAL(19,2)  NOT NULL,
    balance_after     DECIMAL(19,2)  NOT NULL,
    transaction_type  VARCHAR(30)    NOT NULL,
    reference_id      UUID,
    reference_type    VARCHAR(30),
    description       TEXT,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet_transaction PRIMARY KEY (transaction_id)
);

CREATE TABLE IF NOT EXISTS payment.escrow (
    escrow_id         UUID           NOT NULL,
    order_id          UUID           NOT NULL,
    buyer_member_id   UUID           NOT NULL,
    seller_member_id  UUID           NOT NULL,
    amount            DECIMAL(19,2)  NOT NULL,
    original_amount   DECIMAL(19,2)  NOT NULL,
    refunded_amount   DECIMAL(19,2)  NOT NULL,
    escrow_status     VARCHAR(20)    NOT NULL DEFAULT 'HELD',
    refunded_at       TIMESTAMP,
    released_at       TIMESTAMP,
    reference_id      UUID           NOT NULL,
    reference_type    VARCHAR(30)    NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_escrow PRIMARY KEY (escrow_id)
);

CREATE INDEX IF NOT EXISTS idx_escrow_reference
    ON payment.escrow (reference_type, reference_id);

CREATE TABLE IF NOT EXISTS payment.card_transaction (
    card_transaction_id    UUID           NOT NULL,
    transaction_group_id   UUID           NOT NULL,
    related_transaction_id UUID,
    reference_id           UUID           NOT NULL,
    reference_type         VARCHAR(30)    NOT NULL,
    buyer_member_id        UUID           NOT NULL,
    pg_order_id            VARCHAR(100)   NOT NULL,
    pg_payment_key         VARCHAR(200),
    transaction_type       VARCHAR(30)    NOT NULL,
    transaction_status     VARCHAR(30)    NOT NULL,
    cancel_scope           VARCHAR(30),
    requested_amount       DECIMAL(19,2)  NOT NULL,
    approved_amount        DECIMAL(19,2),
    remaining_amount       DECIMAL(19,2),
    reason                 VARCHAR(255),
    failure_code           VARCHAR(100),
    failure_reason         VARCHAR(255),
    requested_at           TIMESTAMP      NOT NULL,
    approved_at            TIMESTAMP,
    failed_at              TIMESTAMP,
    created_at             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_card_transaction PRIMARY KEY (card_transaction_id)
);

CREATE INDEX IF NOT EXISTS idx_card_transaction_group_id
    ON payment.card_transaction (transaction_group_id);
CREATE INDEX IF NOT EXISTS idx_card_transaction_related_transaction_id
    ON payment.card_transaction (related_transaction_id);
CREATE INDEX IF NOT EXISTS idx_card_transaction_reference_id
    ON payment.card_transaction (reference_id);
CREATE INDEX IF NOT EXISTS idx_card_transaction_buyer_member_id
    ON payment.card_transaction (buyer_member_id);
CREATE INDEX IF NOT EXISTS idx_card_transaction_pg_order_id
    ON payment.card_transaction (pg_order_id);

CREATE TABLE IF NOT EXISTS payment.payment_refund (
    refund_id                 UUID           NOT NULL,
    order_cancel_request_id   UUID           NOT NULL,
    order_id                  UUID           NOT NULL,
    buyer_member_id           UUID           NOT NULL,
    refund_type               VARCHAR(30)    NOT NULL,
    payment_method            VARCHAR(30)    NOT NULL,
    total_refund_amount       DECIMAL(19,2)  NOT NULL,
    refund_status             VARCHAR(30)    NOT NULL,
    refund_reason             VARCHAR(255),
    created_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at              TIMESTAMP,
    failed_at                 TIMESTAMP,
    CONSTRAINT pk_payment_refund PRIMARY KEY (refund_id),
    CONSTRAINT uq_payment_refund_order_cancel_request_id UNIQUE (order_cancel_request_id),
    CONSTRAINT chk_payment_refund_total_amount_non_negative CHECK (total_refund_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_payment_refund_order_id
    ON payment.payment_refund (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_refund_buyer_member_id
    ON payment.payment_refund (buyer_member_id);
CREATE INDEX IF NOT EXISTS idx_payment_refund_status
    ON payment.payment_refund (refund_status);

CREATE TABLE IF NOT EXISTS payment.payment_refund_item (
    refund_item_id     UUID           NOT NULL,
    refund_id          UUID           NOT NULL,
    order_item_id      UUID           NOT NULL,
    refund_amount      DECIMAL(19,2)  NOT NULL,
    status             VARCHAR(30)    NOT NULL,
    failure_reason     VARCHAR(255),
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payment_refund_item PRIMARY KEY (refund_item_id),
    CONSTRAINT fk_payment_refund_item_refund_id
        FOREIGN KEY (refund_id) REFERENCES payment.payment_refund (refund_id),
    CONSTRAINT uq_payment_refund_item_refund_order_item UNIQUE (refund_id, order_item_id),
    CONSTRAINT chk_payment_refund_item_amount_non_negative CHECK (refund_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_payment_refund_item_refund_id
    ON payment.payment_refund_item (refund_id);
CREATE INDEX IF NOT EXISTS idx_payment_refund_item_order_item_id
    ON payment.payment_refund_item (order_item_id);

CREATE TABLE IF NOT EXISTS payment.escrow_transaction (
    escrow_transaction_id  UUID           NOT NULL,
    escrow_id              UUID           NOT NULL,
    order_id               UUID           NOT NULL,
    order_item_id          UUID,
    seller_member_id       UUID           NOT NULL,
    buyer_member_id        UUID           NOT NULL,
    transaction_type       VARCHAR(30)    NOT NULL,
    amount                 DECIMAL(19,2)  NOT NULL,
    before_amount          DECIMAL(19,2)  NOT NULL,
    after_amount           DECIMAL(19,2)  NOT NULL,
    reference_id           UUID,
    reference_type         VARCHAR(30),
    description            VARCHAR(255),
    occurred_at            TIMESTAMP      NOT NULL,
    created_at             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
CREATE INDEX IF NOT EXISTS idx_escrow_transaction_transaction_type
    ON payment.escrow_transaction (transaction_type);

CREATE TABLE IF NOT EXISTS payment.order_payment (
    order_payment_id  UUID           NOT NULL,
    order_id          UUID           NOT NULL,
    buyer_member_id   UUID           NOT NULL,
    total_amount      DECIMAL(19,2)  NOT NULL,
    payment_method    VARCHAR(20)    NOT NULL,
    payment_status    VARCHAR(30)    NOT NULL,
    paid_at           TIMESTAMP,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    allocation_id             UUID           NOT NULL,
    order_payment_id          UUID           NOT NULL,
    method                    VARCHAR(20)    NOT NULL,
    amount                    DECIMAL(19,2)  NOT NULL,
    card_transaction_group_id UUID,
    wallet_transaction_id     UUID,
    created_at                TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    refund_allocation_id            UUID           NOT NULL,
    refund_id                       UUID           NOT NULL,
    method                          VARCHAR(20)    NOT NULL,
    amount                          DECIMAL(19,2)  NOT NULL,
    card_cancel_transaction_group_id UUID,
    wallet_refund_transaction_id    UUID,
    created_at                      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

CREATE TABLE IF NOT EXISTS payment.withdraw_request (
    withdraw_request_id        UUID           NOT NULL,
    member_id                  UUID           NOT NULL,
    wallet_id                  UUID           NOT NULL,
    amount                     DECIMAL(19,2)  NOT NULL,
    fee                        DECIMAL(19,2)  NOT NULL,
    actual_amount              DECIMAL(19,2)  NOT NULL,
    encrypted_bank_account     VARCHAR(500)   NOT NULL,
    encrypted_account_holder   VARCHAR(500)   NOT NULL,
    masked_bank_account        VARCHAR(100)   NOT NULL,
    status                     VARCHAR(30)    NOT NULL,
    failure_reason             VARCHAR(500),
    wallet_transaction_id      UUID,
    requested_at               TIMESTAMP      NOT NULL,
    processed_at               TIMESTAMP,
    created_at                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_withdraw_request PRIMARY KEY (withdraw_request_id),
    CONSTRAINT chk_withdraw_request_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_withdraw_request_fee_non_negative CHECK (fee >= 0),
    CONSTRAINT chk_withdraw_request_actual_amount_positive CHECK (actual_amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_withdraw_request_member_id_requested_at
    ON payment.withdraw_request (member_id, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_withdraw_request_wallet_id_requested_at
    ON payment.withdraw_request (wallet_id, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_withdraw_request_status_requested_at
    ON payment.withdraw_request (status, requested_at DESC);

CREATE TABLE IF NOT EXISTS payment.outbox_events (
    id                  UUID           PRIMARY KEY,
    topic               VARCHAR(200)   NOT NULL,
    event_type          VARCHAR(100)   NOT NULL,
    aggregate_id        VARCHAR(255)   NOT NULL,
    trace_id            VARCHAR(255),
    payload             TEXT           NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    retry_count         INTEGER        NOT NULL DEFAULT 0,
    last_error_message  VARCHAR(1000),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payment_outbox_events_status_created_at
    ON payment.outbox_events (status, created_at);

CREATE TABLE IF NOT EXISTS payment.auction_deposit (
    auction_deposit_id            UUID           NOT NULL,
    auction_id                    UUID           NOT NULL,
    bid_id                        UUID           NOT NULL,
    bidder_id                     UUID           NOT NULL,
    deposit_amount                DECIMAL(19,2)  NOT NULL,
    status                        VARCHAR(20)    NOT NULL,
    hold_wallet_transaction_id    UUID           NOT NULL,
    refund_wallet_transaction_id  UUID,
    created_at                    TIMESTAMP      NOT NULL,
    updated_at                    TIMESTAMP      NOT NULL,
    CONSTRAINT pk_auction_deposit PRIMARY KEY (auction_deposit_id)
);

CREATE INDEX IF NOT EXISTS idx_auction_deposit_auction_id
    ON payment.auction_deposit (auction_id);
CREATE INDEX IF NOT EXISTS idx_auction_deposit_auction_id_status
    ON payment.auction_deposit (auction_id, status);
CREATE INDEX IF NOT EXISTS idx_auction_deposit_bid_id
    ON payment.auction_deposit (bid_id);
