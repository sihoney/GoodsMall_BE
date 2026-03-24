package com.example.payment.domain.exception;

public class InvalidChargeRequestException extends RuntimeException {

    public InvalidChargeRequestException(String message) {
        super(message);
    }
}
