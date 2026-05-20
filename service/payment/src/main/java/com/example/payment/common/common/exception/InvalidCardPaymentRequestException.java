package com.example.payment.common.common.exception;

public class InvalidCardPaymentRequestException extends CustomException {

    public InvalidCardPaymentRequestException(String message) {
        super(ErrorCode.INVALID_CARD_PAYMENT_REQUEST, message);
    }

    public InvalidCardPaymentRequestException(String message, Throwable cause) {
        super(ErrorCode.INVALID_CARD_PAYMENT_REQUEST, message, cause);
    }
}
