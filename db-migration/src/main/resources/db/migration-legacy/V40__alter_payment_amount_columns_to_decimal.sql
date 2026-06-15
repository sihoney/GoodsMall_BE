-- payment 금액 컬럼 BIGINT -> DECIMAL(19,2)

ALTER TABLE payment.wallet
    ALTER COLUMN balance TYPE DECIMAL(19,2)
    USING balance::DECIMAL(19,2);

ALTER TABLE payment.charge
    ALTER COLUMN requested_amount TYPE DECIMAL(19,2)
    USING requested_amount::DECIMAL(19,2),
    ALTER COLUMN approved_amount TYPE DECIMAL(19,2)
    USING approved_amount::DECIMAL(19,2);

ALTER TABLE payment.card_transaction
    ALTER COLUMN requested_amount TYPE DECIMAL(19,2)
    USING requested_amount::DECIMAL(19,2),
    ALTER COLUMN approved_amount TYPE DECIMAL(19,2)
    USING approved_amount::DECIMAL(19,2),
    ALTER COLUMN remaining_amount TYPE DECIMAL(19,2)
    USING remaining_amount::DECIMAL(19,2);

ALTER TABLE payment.payment_refund
    ALTER COLUMN total_refund_amount TYPE DECIMAL(19,2)
    USING total_refund_amount::DECIMAL(19,2);

ALTER TABLE payment.payment_refund_item
    ALTER COLUMN refund_amount TYPE DECIMAL(19,2)
    USING refund_amount::DECIMAL(19,2);

ALTER TABLE payment.wallet_transaction
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2),
    ALTER COLUMN balance_after TYPE DECIMAL(19,2)
    USING balance_after::DECIMAL(19,2);

ALTER TABLE payment.escrow
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2),
    ALTER COLUMN original_amount TYPE DECIMAL(19,2)
    USING original_amount::DECIMAL(19,2),
    ALTER COLUMN refunded_amount TYPE DECIMAL(19,2)
    USING refunded_amount::DECIMAL(19,2);

ALTER TABLE payment.escrow_transaction
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2),
    ALTER COLUMN before_amount TYPE DECIMAL(19,2)
    USING before_amount::DECIMAL(19,2),
    ALTER COLUMN after_amount TYPE DECIMAL(19,2)
    USING after_amount::DECIMAL(19,2);

ALTER TABLE payment.order_payment
    ALTER COLUMN total_amount TYPE DECIMAL(19,2)
    USING total_amount::DECIMAL(19,2);

ALTER TABLE payment.order_payment_allocation
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2);

ALTER TABLE payment.payment_refund_allocation
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2);

ALTER TABLE payment.withdraw_request
    ALTER COLUMN amount TYPE DECIMAL(19,2)
    USING amount::DECIMAL(19,2),
    ALTER COLUMN fee TYPE DECIMAL(19,2)
    USING fee::DECIMAL(19,2),
    ALTER COLUMN actual_amount TYPE DECIMAL(19,2)
    USING actual_amount::DECIMAL(19,2);

