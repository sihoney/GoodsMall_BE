ALTER TABLE settlement.settlement_item
    ADD COLUMN settlement_item_status VARCHAR(30) NOT NULL DEFAULT 'UNASSIGNED';

UPDATE settlement.settlement_item
SET settlement_item_status = CASE
    WHEN settlement_id IS NULL THEN 'UNASSIGNED'
    ELSE 'ASSIGNED'
END;

ALTER TABLE settlement.settlement_item
    ALTER COLUMN settlement_item_status DROP DEFAULT;
