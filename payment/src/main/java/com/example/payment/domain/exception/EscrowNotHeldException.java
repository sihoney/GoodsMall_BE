package com.example.payment.domain.exception;

public class EscrowNotHeldException extends EscrowStateException {

    public EscrowNotHeldException(String message) {
        super(message);
    }
}
