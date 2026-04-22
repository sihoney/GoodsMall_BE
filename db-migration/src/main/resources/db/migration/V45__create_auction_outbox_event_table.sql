-- 경매 Outbox 이벤트 테이블
-- BidCreateService에서 Kafka 직접 발행 대신 이 테이블에 이벤트를 저장하고,
-- 별도 릴레이어가 읽어서 Kafka로 발행한다. (Transactional Outbox Pattern)
CREATE TABLE IF NOT EXISTS auction.outbox_event
(
    id            UUID        PRIMARY KEY,
    aggregate_id  UUID        NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    partition_key VARCHAR(100),
    payload       TEXT        NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP   NOT NULL,
    published_at  TIMESTAMP,

    CONSTRAINT chk_outbox_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_status_created_at ON auction.outbox_event (status, created_at);
CREATE INDEX idx_outbox_aggregate_id      ON auction.outbox_event (aggregate_id);