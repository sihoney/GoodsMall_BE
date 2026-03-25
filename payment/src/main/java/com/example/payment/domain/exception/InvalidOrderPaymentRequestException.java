package com.example.payment.domain.exception;

public class InvalidOrderPaymentRequestException extends RuntimeException {

    public InvalidOrderPaymentRequestException(String message) {
        super(message);
    }
}
