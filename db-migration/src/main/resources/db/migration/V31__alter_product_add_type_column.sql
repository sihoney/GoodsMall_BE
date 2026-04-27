ALTER TABLE product.product
    ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'GENERAL'
        CHECK (type IN ('GENERAL', 'AUCTION'));

CREATE INDEX IF NOT EXISTS idx_product_type ON product.product (type);