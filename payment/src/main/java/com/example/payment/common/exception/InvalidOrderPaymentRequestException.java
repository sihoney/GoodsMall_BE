package com.example.payment.common.exception;

public class InvalidOrderPaymentRequestException extends CustomException {

    public InvalidOrderPaymentRequestException(String message) {
        super(ErrorCode.INVALID_ORDER_PAYMENT_REQUEST, message);
    }
}
