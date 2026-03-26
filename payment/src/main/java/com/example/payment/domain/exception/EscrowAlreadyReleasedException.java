package com.example.payment.domain.exception;

public class EscrowAlreadyReleasedException extends EscrowStateException {

    public EscrowAlreadyReleasedException() {
        super("Escrow has already been released.");
    }
}
