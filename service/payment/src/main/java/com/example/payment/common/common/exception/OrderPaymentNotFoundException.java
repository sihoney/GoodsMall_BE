package com.example.payment.common.common.exception;

public class OrderPaymentNotFoundException extends CustomException {

    public OrderPaymentNotFoundException() {
        super(ErrorCode.ORDER_PAYMENT_NOT_FOUND);
    }
}
