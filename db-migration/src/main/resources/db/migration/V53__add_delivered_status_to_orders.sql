ALTER TABLE order_service.orders
    DROP CONSTRAINT chk_order_status;

ALTER TABLE order_service.orders
    ADD CONSTRAINT chk_order_status CHECK (
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
    );
