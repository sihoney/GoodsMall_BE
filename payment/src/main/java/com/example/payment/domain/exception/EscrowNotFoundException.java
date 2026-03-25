package com.example.payment.domain.exception;

public class EscrowNotFoundException extends RuntimeException {

    public EscrowNotFoundException() {
        super("Escrow not found.");
    }
}
