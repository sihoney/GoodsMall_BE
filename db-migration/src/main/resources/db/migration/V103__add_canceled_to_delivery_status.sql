ALTER TABLE order_service.deliveries
    DROP CONSTRAINT IF EXISTS chk_delivery_status;

ALTER TABLE order_service.deliveries
    ADD CONSTRAINT chk_delivery_status
        CHECK (
            delivery_status IN (
                                'PREPARING',
                                'SHIPPED',
                                'DELIVERED',
                                'CANCELED'
                )
            );
