ALTER TABLE order_service.orders
    ALTER COLUMN address DROP NOT NULL,
    ALTER COLUMN address_detail DROP NOT NULL,
    ALTER COLUMN zip_code DROP NOT NULL,
    ALTER COLUMN receiver DROP NOT NULL,
    ALTER COLUMN receiver_phone DROP NOT NULL;
