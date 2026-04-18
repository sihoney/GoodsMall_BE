CREATE TABLE IF NOT EXISTS payment.withdraw_request (
    withdraw_request_id   UUID         NOT NULL,
    member_id             UUID         NOT NULL,
    wallet_id             UUID         NOT NULL,
    amount                BIGINT       NOT NULL,
    fee                   BIGINT       NOT NULL,
    actual_amount         BIGINT       NOT NULL,
    bank_account          VARCHAR(100) NOT NULL,
    account_holder        VARCHAR(100) NOT NULL,
    status                VARCHAR(30)  NOT NULL,
    failure_reason        VARCHAR(500),
    wallet_transaction_id UUID,
    requested_at          TIMESTAMP    NOT NULL,
    processed_at          TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
