UPDATE payment.charge
SET charge_status = 'CONFIRM_SUCCESS'
WHERE charge_status = 'SUCCESS';

UPDATE payment.charge
SET charge_status = 'CONFIRM_FAILED'
WHERE charge_status = 'FAILED';
