-- return_requests 테이블
CREATE TABLE IF NOT EXISTS order_service.return_requests
(
    return_request_id       UUID PRIMARY KEY NOT NULL,
    claim_id                UUID             NOT NULL,
    order_item_id           UUID             NOT NULL,
    seller_id               UUID             NOT NULL,
    carrier                 VARCHAR(50),
    tracking_number         VARCHAR(100),
    status                  VARCHAR(30)      NOT NULL,
    pickup_type             VARCHAR(20),
    pickup_requested_at     TIMESTAMP,
    picked_up_at            TIMESTAMP,
    received_at             TIMESTAMP,
    return_completed_at     TIMESTAMP,
    fail_reason             VARCHAR(500),
    return_address_snapshot VARCHAR(500),
    inspection_status       VARCHAR(20),
    inspection_result       VARCHAR(10),
    created_at              TIMESTAMP        NOT NULL,
    updated_at              TIMESTAMP        NOT NULL,

    CONSTRAINT fk_return_request_claim
        FOREIGN KEY (claim_id) REFERENCES order_service.claims (claim_id),

    CONSTRAINT fk_return_request_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_items (order_item_id),

    CONSTRAINT chk_return_request_status
        CHECK (status IN ('REQUESTED', 'PICKUP_REQUESTED', 'PICKED_UP', 'RECEIVED', 'COMPLETED', 'FAILED')),

    CONSTRAINT chk_return_request_pickup_type
        CHECK (pickup_type IN ('SELF_RETURN', 'PICKUP_REQUEST')),

    CONSTRAINT chk_return_request_inspection_status
        CHECK (inspection_status IN ('PENDING', 'COMPLETED')),

    CONSTRAINT chk_return_request_inspection_result
        CHECK (inspection_result IN ('PASS', 'FAIL'))
);

CREATE INDEX IF NOT EXISTS idx_return_request_claim_id ON order_service.return_requests (claim_id);
CREATE INDEX IF NOT EXISTS idx_return_request_order_item_id ON order_service.return_requests (order_item_id);
CREATE INDEX IF NOT EXISTS idx_return_request_seller_id ON order_service.return_requests (seller_id);