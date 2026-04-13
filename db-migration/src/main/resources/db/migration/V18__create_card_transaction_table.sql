CREATE TABLE IF NOT EXISTS payment.card_transaction (
    card_transaction_id    UUID         NOT NULL,
    transaction_group_id   UUID         NOT NULL,
    related_transaction_id UUID,
    reference_id           UUID         NOT NULL,
    reference_type         VARCHAR(30)  NOT NULL,
    member_id              UUID         NOT NULL,
    pg_order_id            VARCHAR(100) NOT NULL,
    pg_payment_key         VARCHAR(200),
    transaction_type       VARCHAR(30)  NOT NULL,
    transaction_status     VARCHAR(30)  NOT NULL,
    cancel_scope           VARCHAR(30),
    requested_amount       BIGINT       NOT NULL,
    approved_amount        BIGINT,
    remaining_amount       BIGINT,
    reason                 VARCHAR(255),
    failure_code           VARCHAR(100),
    failure_reason         VARCHAR(255),
    requested_at           TIMESTAMP    NOT NULL,
    approved_at            TIMESTAMP,
    failed_at              TIMESTAMP,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_card_transaction PRIMARY KEY (card_transaction_id)
);

CREATE INDEX IF NOT EXISTS idx_card_transaction_group_id
    ON payment.card_transaction (transaction_group_id);

CREATE INDEX IF NOT EXISTS idx_card_transaction_related_transaction_id
    ON payment.card_transaction (related_transaction_id);

CREATE INDEX IF NOT EXISTS idx_card_transaction_reference_id
    ON payment.card_transaction (reference_id);

CREATE INDEX IF NOT EXISTS idx_card_transaction_member_id
    ON payment.card_transaction (member_id);

CREATE INDEX IF NOT EXISTS idx_card_transaction_pg_order_id
    ON payment.card_transaction (pg_order_id);
