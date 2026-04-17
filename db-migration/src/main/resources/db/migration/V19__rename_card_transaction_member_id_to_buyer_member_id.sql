DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'payment'
          AND table_name = 'card_transaction'
          AND column_name = 'member_id'
    ) THEN
        ALTER TABLE payment.card_transaction
            RENAME COLUMN member_id TO buyer_member_id;
    END IF;
END $$;

DROP INDEX IF EXISTS payment.idx_card_transaction_member_id;

CREATE INDEX IF NOT EXISTS idx_card_transaction_buyer_member_id
    ON payment.card_transaction (buyer_member_id);
