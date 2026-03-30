CREATE TABLE IF NOT EXISTS payment.wallet (
    wallet_id   UUID        NOT NULL,
    member_id   UUID        NOT NULL,
    balance     BIGINT      NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet PRIMARY KEY (wallet_id),
    CONSTRAINT uq_wallet_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS payment.charge (
    charge_id        UUID         NOT NULL,
    member_id        UUID         NOT NULL,
    wallet_id        UUID,
    requested_amount BIGINT       NOT NULL,
    approved_amount  BIGINT,
    pg_provider      VARCHAR(20)  NOT NULL,
    pg_order_id      VARCHAR(100) NOT NULL,
    pg_payment_key   VARCHAR(200),
    charge_status    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    requested_at     TIMESTAMP    NOT NULL,
    approved_at      TIMESTAMP,
    failed_at        TIMESTAMP,
    failure_reason   VARCHAR(500),
    CONSTRAINT pk_charge PRIMARY KEY (charge_id),
    CONSTRAINT uq_charge_pg_order_id UNIQUE (pg_order_id)
);

CREATE TABLE IF NOT EXISTS payment.charge_refund (
    charge_refund_id UUID         NOT NULL,
    charge_id        UUID         NOT NULL,
    refund_amount    BIGINT       NOT NULL,
    refund_reason    VARCHAR(255) NOT NULL,
    refund_status    VARCHAR(30)  NOT NULL,
    requested_at     TIMESTAMP    NOT NULL,
    refunded_at      TIMESTAMP,
    failed_at        TIMESTAMP,
    failure_reason   VARCHAR(500),
    CONSTRAINT pk_charge_refund PRIMARY KEY (charge_refund_id)
);

CREATE TABLE IF NOT EXISTS payment.wallet_transaction (
    transaction_id   UUID         NOT NULL,
    wallet_id        UUID         NOT NULL,
    amount           BIGINT       NOT NULL,
    balance_after    BIGINT       NOT NULL,
    transaction_type VARCHAR(30)  NOT NULL,
    reference_id     UUID,
    reference_type   VARCHAR(30),
    description      TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet_transaction PRIMARY KEY (transaction_id)
);

CREATE TABLE IF NOT EXISTS payment.escrow (
    escrow_id        UUID        NOT NULL,
    order_id         UUID        NOT NULL,
    buyer_member_id  UUID        NOT NULL,
    seller_member_id UUID        NOT NULL,
    amount           BIGINT      NOT NULL,
    escrow_status    VARCHAR(20) NOT NULL DEFAULT 'HELD',
    refunded_at      TIMESTAMP,
    released_at      TIMESTAMP,
    release_at       TIMESTAMP,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT pk_escrow PRIMARY KEY (escrow_id)
);
