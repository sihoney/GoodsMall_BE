-- settlement 스키마의 기준 테이블을 만드는 파일입니다.
-- 팀에서 같이 작업할 때는 아래 규칙을 맞춰 주세요.
-- 1) 이미 적용된 migration 파일은 다시 덮어쓰지 않습니다.
-- 2) 엔티티/테이블 변경은 새 버전 migration(V*.sql)으로 추가합니다.
-- 3) 스키마 변경과 seed/백필 작업은 가능하면 분리해서 관리합니다.
-- TODO(팀): settlement 변경은 이 파일이 아니라 V5+ 신규 migration으로 이어서 작성합니다.

CREATE TABLE IF NOT EXISTS settlement.settlement (
    settlement_id            UUID         NOT NULL,
    seller_id                UUID         NOT NULL,
    settlement_year          INT          NOT NULL,
    settlement_month         INT          NOT NULL,
    total_sales_amount       BIGINT       NOT NULL DEFAULT 0,
    fee_amount               BIGINT       NOT NULL DEFAULT 0,
    final_settlement_amount  BIGINT       NOT NULL DEFAULT 0,
    settled_amount           BIGINT       NOT NULL DEFAULT 0,
    settlement_status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    settled_at               TIMESTAMP,
    last_failure_reason      VARCHAR(500),
    requested_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement PRIMARY KEY (settlement_id)
);

CREATE TABLE IF NOT EXISTS settlement.settlement_item (
    settlement_item_id UUID      NOT NULL,
    settlement_id      UUID,
    order_id           UUID      NOT NULL,
    escrow_id          UUID      NOT NULL,
    seller_id          UUID      NOT NULL,
    gross_amount       BIGINT    NOT NULL,
    fee_amount         BIGINT    NOT NULL,
    net_amount         BIGINT    NOT NULL,
    released_at        TIMESTAMP NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_item PRIMARY KEY (settlement_item_id),
    CONSTRAINT uq_settlement_item_escrow_id UNIQUE (escrow_id)
);
