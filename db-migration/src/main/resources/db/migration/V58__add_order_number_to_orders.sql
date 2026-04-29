ALTER TABLE order_service.orders
    ADD COLUMN order_number VARCHAR(14) NOT NULL DEFAULT '';

UPDATE order_service.orders
SET order_number = TO_CHAR(created_at, 'YYMMDD') || LPAD(FLOOR(RANDOM() * 100000000)::TEXT, 8, '0')
WHERE order_number = '';

ALTER TABLE order_service.orders
    ADD CONSTRAINT uq_orders_order_number UNIQUE (order_number);

ALTER TABLE order_service.orders
    ALTER COLUMN order_number DROP DEFAULT;
