ALTER TABLE settlement.settlement
    ADD COLUMN settlement_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY';

CREATE INDEX IF NOT EXISTS idx_settlement_seller_year_month_type
    ON settlement.settlement (seller_id, settlement_year, settlement_month, settlement_type);
