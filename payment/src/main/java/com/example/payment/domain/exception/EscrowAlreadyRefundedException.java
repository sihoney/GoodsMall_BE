package com.example.payment.domain.exception;

public class EscrowAlreadyRefundedException extends EscrowStateException {

    public EscrowAlreadyRefundedException() {
        super("Escrow has already been refunded.");
    }
}
