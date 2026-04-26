CREATE TABLE payment.outbox_events (
    id UUID PRIMARY KEY,
    topic VARCHAR(200) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    trace_id VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

CREATE INDEX idx_payment_outbox_events_status_created_at
    ON payment.outbox_events (status, created_at);
