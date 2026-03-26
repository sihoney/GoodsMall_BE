package com.example.payment.common.exception;

public class ChargeNotFoundException extends CustomException {

    public ChargeNotFoundException() {
        super(ErrorCode.CHARGE_NOT_FOUND);
    }
}
