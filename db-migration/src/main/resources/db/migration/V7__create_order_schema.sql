-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V13, V14, V53, V54, V55, V56, V57, V58, V59, V60,
-- V101, V103
--
-- Explicitly excluded data-fix migrations:
-- V102
CREATE SCHEMA IF NOT EXISTS order_service;

CREATE TABLE IF NOT EXISTS order_service.orders (
    order_id                     UUID           NOT NULL,
    buyer_id                     UUID           NOT NULL,
    total_price                  DECIMAL(19,2)  NOT NULL,
    order_status                 VARCHAR(30)    NOT NULL,
    order_type                   VARCHAR(20)    NOT NULL,
    order_number                 VARCHAR(14)    NOT NULL,
    auction_id                   UUID,
    address                      VARCHAR(255),
    address_detail               VARCHAR(255),
    zip_code                     VARCHAR(20),
    receiver                     VARCHAR(100),
    receiver_phone               VARCHAR(30),
    representative_product_name  VARCHAR(255)   NOT NULL,
    representative_thumbnail_key VARCHAR(255),
    item_count                   INTEGER        NOT NULL,
    delivered_at                 TIMESTAMP,
    created_at                   TIMESTAMP      NOT NULL,
    updated_at                   TIMESTAMP      NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT chk_order_status
        CHECK (
            order_status IN (
                'CREATED',
                'CONFIRMED',
                'SHIPPING',
                'PARTIAL_SHIPPING',
                'DELIVERED',
                'COMPLETED',
                'PARTIAL_CANCELED',
                'CANCELED'
            )
        ),
    CONSTRAINT chk_order_type
        CHECK (order_type IN ('NORMAL', 'AUCTION'))
);

CREATE INDEX IF NOT EXISTS idx_order_buyer_id
    ON order_service.orders (buyer_id);

CREATE TABLE IF NOT EXISTS order_service.order_items (
    order_item_id          UUID           NOT NULL,
    product_id             UUID           NOT NULL,
    order_id               UUID           NOT NULL,
    seller_id              UUID           NOT NULL,
    product_name_snapshot  VARCHAR(255)   NOT NULL,
    unit_price_snapshot    DECIMAL(19,2)  NOT NULL,
    quantity               INTEGER        NOT NULL,
    order_item_status      VARCHAR(30)    NOT NULL,
    thumbnail_key_snapshot VARCHAR(255),
    created_at             TIMESTAMP      NOT NULL,
    updated_at             TIMESTAMP      NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES order_service.orders (order_id),
    CONSTRAINT chk_order_item_status
        CHECK (
            order_item_status IN (
                'PENDING',
                'PREPARING',
                'SHIPPING',
                'DELIVERED',
                'COMPLETED',
                'CANCELED',
                'RETURN_REQUESTED'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id
    ON order_service.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_seller_id
    ON order_service.order_items (seller_id);

CREATE TABLE IF NOT EXISTS order_service.deliveries (
    delivery_id      UUID         NOT NULL,
    seller_id        UUID         NOT NULL,
    buyer_id         UUID         NOT NULL,
    order_item_id    UUID         NOT NULL,
    courier_code     VARCHAR(50),
    invoice_number   VARCHAR(100),
    delivery_status  VARCHAR(30)  NOT NULL,
    shipped_at       TIMESTAMP,
    delivered_at     TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_deliveries PRIMARY KEY (delivery_id),
    CONSTRAINT fk_deliveries_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_items (order_item_id),
    CONSTRAINT uq_deliveries_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_delivery_status
        CHECK (
            delivery_status IN (
                'PREPARING',
                'SHIPPED',
                'DELIVERED',
                'CANCELED'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_deliveries_order_item_id
    ON order_service.deliveries (order_item_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_seller_id
    ON order_service.deliveries (seller_id);

CREATE TABLE IF NOT EXISTS order_service.claims (
    claim_id             UUID          NOT NULL,
    order_item_id        UUID          NOT NULL,
    seller_id            UUID          NOT NULL,
    type                 VARCHAR(20)   NOT NULL,
    reason               VARCHAR(100)  NOT NULL,
    detail_reason        VARCHAR(500),
    status               VARCHAR(20)   NOT NULL,
    requester_type       VARCHAR(20)   NOT NULL,
    responsibility_type  VARCHAR(20),
    reject_reason        VARCHAR(500),
    requested_at         TIMESTAMP     NOT NULL,
    completed_at         TIMESTAMP,
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL,
    CONSTRAINT pk_claims PRIMARY KEY (claim_id),
    CONSTRAINT fk_claim_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_items (order_item_id),
    CONSTRAINT chk_claim_type
        CHECK (type IN ('CANCEL', 'RETURN', 'EXCHANGE')),
    CONSTRAINT chk_claim_status
        CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED')),
    CONSTRAINT chk_claim_requester_type
        CHECK (requester_type IN ('BUYER', 'SELLER', 'ADMIN')),
    CONSTRAINT chk_claim_responsibility_type
        CHECK (responsibility_type IN ('BUYER', 'SELLER', 'ADMIN'))
);

CREATE INDEX IF NOT EXISTS idx_claim_order_item_id
    ON order_service.claims (order_item_id);
CREATE INDEX IF NOT EXISTS idx_claim_seller_id
    ON order_service.claims (seller_id);

CREATE TABLE IF NOT EXISTS order_service.return_requests (
    return_request_id       UUID          NOT NULL,
    claim_id                UUID          NOT NULL,
    order_item_id           UUID          NOT NULL,
    seller_id               UUID          NOT NULL,
    carrier                 VARCHAR(50),
    tracking_number         VARCHAR(100),
    status                  VARCHAR(30)   NOT NULL,
    pickup_type             VARCHAR(20),
    pickup_requested_at     TIMESTAMP,
    picked_up_at            TIMESTAMP,
    received_at             TIMESTAMP,
    return_completed_at     TIMESTAMP,
    fail_reason             VARCHAR(500),
    return_address_snapshot VARCHAR(500),
    inspection_status       VARCHAR(20),
    inspection_result       VARCHAR(10),
    created_at              TIMESTAMP     NOT NULL,
    updated_at              TIMESTAMP     NOT NULL,
    CONSTRAINT pk_return_requests PRIMARY KEY (return_request_id),
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

CREATE INDEX IF NOT EXISTS idx_return_request_claim_id
    ON order_service.return_requests (claim_id);
CREATE INDEX IF NOT EXISTS idx_return_request_order_item_id
    ON order_service.return_requests (order_item_id);
CREATE INDEX IF NOT EXISTS idx_return_request_seller_id
    ON order_service.return_requests (seller_id);

CREATE TABLE IF NOT EXISTS order_service.outbox_events (
    id            UUID         NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    aggregate_id  VARCHAR(255) NOT NULL,
    payload       TEXT         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count   INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL,
    published_at  TIMESTAMP,
    CONSTRAINT pk_order_outbox_events PRIMARY KEY (id),
    CONSTRAINT chk_order_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_order_outbox_status_created_at
    ON order_service.outbox_events (status, created_at);

CREATE TABLE IF NOT EXISTS order_service.couriers (
    code    VARCHAR(20)  NOT NULL,
    name    VARCHAR(50)  NOT NULL,
    active  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_couriers PRIMARY KEY (code)
);
