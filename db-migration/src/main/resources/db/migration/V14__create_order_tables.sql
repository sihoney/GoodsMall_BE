-- orders 테이블
CREATE TABLE IF NOT EXISTS order_service.orders
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

-- order_item 테이블
CREATE TABLE IF NOT EXISTS order_service.order_item
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
        FOREIGN KEY (order_id) REFERENCES order_service.orders (order_id),

    CONSTRAINT chk_order_item_status
        CHECK (
            order_item_status IN (
                                  'PENDING',
                                  'PREPARING',
                                  'SHIPPING',
                                  'DELIVERED',
                                  'CANCELLED'
                )
            )
);

-- delivery 테이블
CREATE TABLE IF NOT EXISTS order_service.delivery
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

    CONSTRAINT fk_delivery_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_service.order_item (order_item_id),

    CONSTRAINT uq_delivery_order_item
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

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_order_buyer_id ON order_service.orders (buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_service.order_item (order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_seller_id ON order_service.order_item (seller_id);
CREATE INDEX IF NOT EXISTS idx_delivery_order_item_id ON order_service.delivery (order_item_id);
CREATE INDEX IF NOT EXISTS idx_delivery_seller_id ON order_service.delivery (seller_id);
