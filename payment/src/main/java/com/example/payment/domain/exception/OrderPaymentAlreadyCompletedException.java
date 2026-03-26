package com.example.payment.domain.exception;

public class OrderPaymentAlreadyCompletedException extends InvalidOrderPaymentRequestException {

    public OrderPaymentAlreadyCompletedException() {
        super("Order payment has already been completed.");
    }
}
