-- settlement 금액 컬럼 BIGINT -> DECIMAL(19,2)

ALTER TABLE settlement.settlement
    ALTER COLUMN total_sales_amount TYPE DECIMAL(19,2)
    USING total_sales_amount::DECIMAL(19,2),
    ALTER COLUMN fee_amount TYPE DECIMAL(19,2)
    USING fee_amount::DECIMAL(19,2),
    ALTER COLUMN final_settlement_amount TYPE DECIMAL(19,2)
    USING final_settlement_amount::DECIMAL(19,2),
    ALTER COLUMN settled_amount TYPE DECIMAL(19,2)
    USING settled_amount::DECIMAL(19,2);

ALTER TABLE settlement.settlement_item
    ALTER COLUMN gross_amount TYPE DECIMAL(19,2)
    USING gross_amount::DECIMAL(19,2),
    ALTER COLUMN fee_amount TYPE DECIMAL(19,2)
    USING fee_amount::DECIMAL(19,2),
    ALTER COLUMN net_amount TYPE DECIMAL(19,2)
    USING net_amount::DECIMAL(19,2);

