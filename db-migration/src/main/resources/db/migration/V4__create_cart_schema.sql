-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V7, V8, V9, V10
CREATE SCHEMA IF NOT EXISTS cart;

CREATE TABLE IF NOT EXISTS cart.cart (
    cart_id     UUID         NOT NULL,
    member_id   UUID         NOT NULL,
    product_id  UUID         NOT NULL,
    quantity    INTEGER      NOT NULL,
    added_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_cart PRIMARY KEY (cart_id),
    CONSTRAINT uk_cart_member_product UNIQUE (member_id, product_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity >= 1)
);

CREATE INDEX IF NOT EXISTS idx_cart_member_id
    ON cart.cart (member_id);
CREATE INDEX IF NOT EXISTS idx_cart_product_id
    ON cart.cart (product_id);
CREATE INDEX IF NOT EXISTS idx_cart_member_product
    ON cart.cart (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_cart_added_at
    ON cart.cart (added_at DESC);

CREATE TABLE IF NOT EXISTS cart.wish (
    id          UUID         NOT NULL,
    member_id   UUID         NOT NULL,
    product_id  UUID         NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    CONSTRAINT pk_wish PRIMARY KEY (id),
    CONSTRAINT uk_wish_member_product UNIQUE (member_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_wish_member_id
    ON cart.wish (member_id);
CREATE INDEX IF NOT EXISTS idx_wish_product_id
    ON cart.wish (product_id);
CREATE INDEX IF NOT EXISTS idx_wish_member_product
    ON cart.wish (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_wish_created_at
    ON cart.wish (created_at DESC);
