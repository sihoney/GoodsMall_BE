CREATE TABLE order_service.deliveries
(
    delivery_id     UUID PRIMARY KEY NOT NULL,
    seller_id       UUID             NOT NULL,
    buyer_id        UUID             NOT NULL,
    order_item_id   UUID             NOT NULL,
    courier_code    VARCHAR(50),
    invoice_number  VARCHAR(100),
    delivery_status VARCHAR(30)      NOT NULL,
    shipped_at      TIMESTAMP,
    delivered_at    TIMESTAMP,
    created_at      TIMESTAMP        NOT NULL,
    updated_at      TIMESTAMP        NOT NULL,

    CONSTRAINT fk_deliveries_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_items (order_item_id),

    CONSTRAINT uq_deliveries_order_item
        UNIQUE (order_item_id),

    CONSTRAINT chk_delivery_status
        CHECK (
            delivery_status IN (
                                'PREPARING',
                                'SHIPPED',
                                'DELIVERED'
                )
            )
);