ALTER TABLE payment.payment_refund
    DROP CONSTRAINT IF EXISTS chk_payment_refund_total_amount_positive;

ALTER TABLE payment.payment_refund
    ADD CONSTRAINT chk_payment_refund_total_amount_non_negative
        CHECK (total_refund_amount >= 0);

ALTER TABLE payment.payment_refund_item
    DROP CONSTRAINT IF EXISTS chk_payment_refund_item_amount_positive;

ALTER TABLE payment.payment_refund_item
    ADD CONSTRAINT chk_payment_refund_item_amount_non_negative
        CHECK (refund_amount >= 0);
