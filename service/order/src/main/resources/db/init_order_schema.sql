CREATE SCHEMA IF NOT EXISTS order_service;

CREATE TABLE order_service.orders
(
    order_id                     UUID PRIMARY KEY NOT NULL,
    buyer_id                     UUID             NOT NULL,
    total_price                  DECIMAL(19, 2)   NOT NULL,
    order_status                 VARCHAR(30)      NOT NULL,
    created_at                   TIMESTAMP        NOT NULL,
    updated_at                   TIMESTAMP        NOT NULL,
    address                      VARCHAR(255)     NOT NULL,
    address_detail               VARCHAR(255)     NOT NULL,
    zip_code                     VARCHAR(20)      NOT NULL,
    receiver                     VARCHAR(100)     NOT NULL,
    receiver_phone               VARCHAR(30)      NOT NULL,
    representative_product_name  VARCHAR(255)     NOT NULL,
    representative_thumbnail_key VARCHAR(255),
    item_count                   INTEGER          NOT NULL,

    CONSTRAINT chk_order_status
        CHECK (
            order_status IN (
                             'CREATED',
                             'CONFIRMED',
                             'SHIPPING',
                             'PARTIAL_SHIPPING',
                             'COMPLETED',
                             'PARTIAL_CANCELED',
                             'CANCELED'
                )
            )
);