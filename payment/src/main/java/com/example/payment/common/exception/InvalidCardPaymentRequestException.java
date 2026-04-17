package com.example.payment.common.exception;

public class InvalidCardPaymentRequestException extends CustomException {

    public InvalidCardPaymentRequestException(String message) {
        super(ErrorCode.INVALID_CARD_PAYMENT_REQUEST, message);
    }
}
