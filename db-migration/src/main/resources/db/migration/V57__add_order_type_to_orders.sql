ALTER TABLE order_service.orders
    ADD COLUMN order_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE order_service.orders
    ADD CONSTRAINT chk_order_type CHECK (order_type IN ('NORMAL', 'AUCTION'));
