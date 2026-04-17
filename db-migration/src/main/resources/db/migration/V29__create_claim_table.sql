-- claim 테이블
CREATE TABLE IF NOT EXISTS order_service.claims
(
    claim_id            UUID PRIMARY KEY NOT NULL,
    order_item_id       UUID             NOT NULL,
    seller_id           UUID             NOT NULL,
    type                VARCHAR(20)      NOT NULL,
    reason              VARCHAR(100)     NOT NULL,
    detail_reason       VARCHAR(500),
    status              VARCHAR(20)      NOT NULL,
    requester_type      VARCHAR(20)      NOT NULL,
    responsibility_type VARCHAR(20),
    reject_reason       VARCHAR(500),
    requested_at        TIMESTAMP        NOT NULL,
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP        NOT NULL,
    updated_at          TIMESTAMP        NOT NULL,

    CONSTRAINT fk_claim_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_item (order_item_id),

    CONSTRAINT chk_claim_type
        CHECK (type IN ('CANCEL', 'RETURN', 'EXCHANGE')),

    CONSTRAINT chk_claim_status
        CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED')),

    CONSTRAINT chk_claim_requester_type
        CHECK (requester_type IN ('BUYER', 'SELLER', 'ADMIN')),

    CONSTRAINT chk_claim_responsibility_type
        CHECK (responsibility_type IN ('BUYER', 'SELLER', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_claim_order_item_id ON order_service.claims (order_item_id);
CREATE INDEX IF NOT EXISTS idx_claim_seller_id ON order_service.claims (seller_id);