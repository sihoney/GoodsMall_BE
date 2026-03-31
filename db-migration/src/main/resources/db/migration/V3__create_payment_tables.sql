-- payment 스키마의 기준 테이블을 만드는 파일입니다.
-- 팀에서 같이 작업할 때는 아래 규칙만 지켜 주세요.
-- 1) 이미 적용된 migration 파일은 의미가 바뀌는 수정(덮어쓰기)을 하지 않습니다.
-- 2) 엔티티/테이블/컬럼/인덱스 변경은 항상 새 V*.sql 파일로 추가합니다.
-- 3) 배포 안정성을 위해 하위 호환 DDL을 먼저 적용하고, 데이터 정리는 후속 단계로 분리합니다.
-- TODO(팀): payment 변경은 이 파일이 아니라 V5+ 신규 migration으로 이어서 작성합니다.

CREATE TABLE IF NOT EXISTS payment.wallet (
    wallet_id   UUID        NOT NULL,
    member_id   UUID        NOT NULL,
    balance     BIGINT      NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet PRIMARY KEY (wallet_id),
    CONSTRAINT uq_wallet_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS payment.charge (
    charge_id        UUID         NOT NULL,
    member_id        UUID         NOT NULL,
    wallet_id        UUID,
    requested_amount BIGINT       NOT NULL,
    approved_amount  BIGINT,
    pg_provider      VARCHAR(20)  NOT NULL,
    pg_order_id      VARCHAR(100) NOT NULL,
    pg_payment_key   VARCHAR(200),
    charge_status    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    requested_at     TIMESTAMP    NOT NULL,
    approved_at      TIMESTAMP,
    failed_at        TIMESTAMP,
    failure_reason   VARCHAR(500),
    CONSTRAINT pk_charge PRIMARY KEY (charge_id),
    CONSTRAINT uq_charge_pg_order_id UNIQUE (pg_order_id)
);

CREATE TABLE IF NOT EXISTS payment.charge_refund (
    charge_refund_id UUID         NOT NULL,
    charge_id        UUID         NOT NULL,
    refund_amount    BIGINT       NOT NULL,
    refund_reason    VARCHAR(255) NOT NULL,
    refund_status    VARCHAR(30)  NOT NULL,
    requested_at     TIMESTAMP    NOT NULL,
    refunded_at      TIMESTAMP,
    failed_at        TIMESTAMP,
    failure_reason   VARCHAR(500),
    CONSTRAINT pk_charge_refund PRIMARY KEY (charge_refund_id)
);

CREATE TABLE IF NOT EXISTS payment.wallet_transaction (
    transaction_id   UUID         NOT NULL,
    wallet_id        UUID         NOT NULL,
    amount           BIGINT       NOT NULL,
    balance_after    BIGINT       NOT NULL,
    transaction_type VARCHAR(30)  NOT NULL,
    reference_id     UUID,
    reference_type   VARCHAR(30),
    description      TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet_transaction PRIMARY KEY (transaction_id)
);

CREATE TABLE IF NOT EXISTS payment.escrow (
    escrow_id        UUID        NOT NULL,
    order_id         UUID        NOT NULL,
    buyer_member_id  UUID        NOT NULL,
    seller_member_id UUID        NOT NULL,
    amount           BIGINT      NOT NULL,
    escrow_status    VARCHAR(20) NOT NULL DEFAULT 'HELD',
    refunded_at      TIMESTAMP,
    released_at      TIMESTAMP,
    release_at       TIMESTAMP,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT pk_escrow PRIMARY KEY (escrow_id)
);
