ALTER TABLE order_service.order_items
    DROP CONSTRAINT chk_order_item_status;

ALTER TABLE order_service.order_items
    ADD CONSTRAINT chk_order_item_status CHECK (
        order_item_status IN (
            'PENDING',
            'PREPARING',
            'SHIPPING',
            'DELIVERED',
            'COMPLETED',
            'CANCELED',
            'RETURN_REQUESTED'
        )
    );