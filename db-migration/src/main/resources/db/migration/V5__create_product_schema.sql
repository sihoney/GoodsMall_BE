-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V31, V52
CREATE SCHEMA IF NOT EXISTS product;

CREATE TABLE IF NOT EXISTS product.category (
    category_id   UUID         NOT NULL,
    parent_id     UUID,
    seller_id     UUID,
    name          VARCHAR(50)  NOT NULL,
    description   VARCHAR(500),
    depth         INTEGER      NOT NULL,
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    deleted_at    TIMESTAMP,
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES product.category (category_id)
);

CREATE INDEX IF NOT EXISTS idx_category_parent_id
    ON product.category (parent_id);
CREATE INDEX IF NOT EXISTS idx_category_seller_id
    ON product.category (seller_id);
CREATE INDEX IF NOT EXISTS idx_category_deleted_at
    ON product.category (deleted_at);
CREATE INDEX IF NOT EXISTS idx_category_depth
    ON product.category (depth);

CREATE TABLE IF NOT EXISTS product.product (
    product_id       UUID           NOT NULL,
    seller_id        UUID           NOT NULL,
    category_id      UUID           NOT NULL,
    title            VARCHAR(255)   NOT NULL,
    description      TEXT,
    price            DECIMAL(10,2)  NOT NULL,
    stock_quantity   INTEGER        NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    type             VARCHAR(20)    NOT NULL DEFAULT 'GENERAL',
    view_count       INTEGER        NOT NULL DEFAULT 0,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    deleted_at       TIMESTAMP,
    CONSTRAINT pk_product PRIMARY KEY (product_id),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES product.category (category_id),
    CONSTRAINT chk_product_status
        CHECK (status IN ('ACTIVE', 'SOLD_OUT', 'INACTIVE')),
    CONSTRAINT chk_product_type
        CHECK (type IN ('GENERAL', 'AUCTION'))
);

CREATE INDEX IF NOT EXISTS idx_product_seller_id
    ON product.product (seller_id);
CREATE INDEX IF NOT EXISTS idx_product_status
    ON product.product (status);
CREATE INDEX IF NOT EXISTS idx_product_created_at
    ON product.product (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_product_type
    ON product.product (type);

CREATE TABLE IF NOT EXISTS product.product_image (
    image_id       UUID           NOT NULL,
    product_id     UUID           NOT NULL,
    s3_key         VARCHAR(500)   NOT NULL,
    sort_order     INTEGER        NOT NULL,
    is_thumbnail   BOOLEAN        NOT NULL DEFAULT false,
    created_at     TIMESTAMP      NOT NULL,
    CONSTRAINT pk_product_image PRIMARY KEY (image_id),
    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id) REFERENCES product.product (product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_image_product_id
    ON product.product_image (product_id);
CREATE INDEX IF NOT EXISTS idx_product_image_is_thumbnail
    ON product.product_image (is_thumbnail);

CREATE TABLE IF NOT EXISTS product.outbox_event (
    id              UUID           NOT NULL,
    aggregate_id    UUID           NOT NULL,
    aggregate_type  VARCHAR(50)    NOT NULL,
    event_type      VARCHAR(100)   NOT NULL,
    topic           VARCHAR(200)   NOT NULL,
    partition_key   VARCHAR(100),
    payload         TEXT           NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP      NOT NULL,
    published_at    TIMESTAMP,
    CONSTRAINT pk_product_outbox_event PRIMARY KEY (id),
    CONSTRAINT chk_product_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED'))
);

CREATE INDEX IF NOT EXISTS idx_product_outbox_status_created_at
    ON product.outbox_event (status, created_at);
CREATE INDEX IF NOT EXISTS idx_product_outbox_aggregate_id
    ON product.outbox_event (aggregate_id);
