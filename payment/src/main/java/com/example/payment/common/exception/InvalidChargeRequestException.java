package com.example.payment.common.exception;

public class InvalidChargeRequestException extends CustomException {

    public InvalidChargeRequestException(String message) {
        super(ErrorCode.INVALID_CHARGE_REQUEST, message);
    }
}
