CREATE TABLE order_item
(
    order_item_id          UUID PRIMARY KEY NOT NULL,
    product_id             UUID             NOT NULL,
    order_id               UUID             NOT NULL,
    seller_id              UUID             NOT NULL,
    product_name_snapshot  VARCHAR(255)     NOT NULL,
    unit_price_snapshot    DECIMAL(19, 2)   NOT NULL,
    quantity               INTEGER          NOT NULL,
    order_item_status      VARCHAR(30)      NOT NULL,
    thumbnail_key_snapshot VARCHAR(255),
    created_at             TIMESTAMP        NOT NULL,
    updated_at             TIMESTAMP        NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES "order" (order_id),

    CONSTRAINT chk_order_item_status
        CHECK (
            order_item_status IN (
                                  'PENDING',
                                  'PREPARING',
                                  'SHIPPING',
                                  'DELIVERED',
                                  'CANCELED'
                )
            )
);