package com.example.payment.common.exception;

public class PaymentGatewayException extends CustomException {

    public PaymentGatewayException(String message) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(ErrorCode.PAYMENT_GATEWAY_ERROR, message, cause);
    }
}
