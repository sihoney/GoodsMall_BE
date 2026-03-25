package com.example.payment.domain.exception;

public class EscrowStateException extends RuntimeException {

    public EscrowStateException(String message) {
        super(message);
    }
}
