package com.example.payment.domain.exception;

public class EscrowReleaseAlreadyScheduledException extends EscrowStateException {

    public EscrowReleaseAlreadyScheduledException() {
        super("releaseAt has already been scheduled.");
    }
}
