CREATE TABLE IF NOT EXISTS product.outbox_event
(
    id             UUID         PRIMARY KEY,
    aggregate_id   UUID         NOT NULL,
    aggregate_type VARCHAR(50)  NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    topic          VARCHAR(200) NOT NULL,
    partition_key  VARCHAR(100),
    payload        TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP    NOT NULL,
    published_at   TIMESTAMP,

    CONSTRAINT chk_product_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED'))
);

CREATE INDEX IF NOT EXISTS idx_product_outbox_status_created_at ON product.outbox_event (status, created_at);
CREATE INDEX IF NOT EXISTS idx_product_outbox_aggregate_id ON product.outbox_event (aggregate_id);
