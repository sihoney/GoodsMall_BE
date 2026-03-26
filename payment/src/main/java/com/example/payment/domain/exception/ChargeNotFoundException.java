package com.example.payment.domain.exception;

public class ChargeNotFoundException extends RuntimeException {

    public ChargeNotFoundException() {
        super("Charge not found.");
    }
}
