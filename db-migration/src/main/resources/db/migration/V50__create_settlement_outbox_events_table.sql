CREATE TABLE IF NOT EXISTS settlement.outbox_events
(
    id                 UUID         PRIMARY KEY,
    topic              VARCHAR(200) NOT NULL,
    event_type         VARCHAR(100) NOT NULL,
    aggregate_id       VARCHAR(255) NOT NULL,
    trace_id           VARCHAR(255),
    payload            TEXT         NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count        INT          NOT NULL DEFAULT 0,
    last_error_message VARCHAR(1000),
    created_at         TIMESTAMP    NOT NULL,
    published_at       TIMESTAMP,

    CONSTRAINT chk_settlement_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_settlement_outbox_status_created_at
    ON settlement.outbox_events (status, created_at);

CREATE INDEX idx_settlement_outbox_aggregate_id
    ON settlement.outbox_events (aggregate_id);
