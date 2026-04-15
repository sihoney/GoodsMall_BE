package com.example.member.common.exception;

public class AccountVerificationNotFoundException extends RuntimeException {

    public AccountVerificationNotFoundException() {
        super("Account verification session was not found.");
    }

    public AccountVerificationNotFoundException(String message) {
        super(message);
    }
}
