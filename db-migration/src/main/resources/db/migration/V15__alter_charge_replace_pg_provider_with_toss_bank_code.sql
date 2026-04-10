ALTER TABLE payment.charge
    DROP COLUMN IF EXISTS pg_provider;

ALTER TABLE payment.charge
    ADD COLUMN IF NOT EXISTS toss_bank_code VARCHAR(30);
