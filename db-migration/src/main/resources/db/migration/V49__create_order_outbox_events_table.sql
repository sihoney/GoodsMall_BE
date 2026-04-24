CREATE TABLE IF NOT EXISTS order_service.outbox_events
(
    id           UUID         PRIMARY KEY,
    topic        VARCHAR(200) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count  INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL,
    published_at TIMESTAMP,

    CONSTRAINT chk_order_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_order_outbox_status_created_at ON order_service.outbox_events (status, created_at);