CREATE UNIQUE INDEX IF NOT EXISTS uq_monthly_settlement_seller_year_month
    ON settlement.settlement (seller_id, settlement_year, settlement_month)
    WHERE settlement_type = 'MONTHLY';
