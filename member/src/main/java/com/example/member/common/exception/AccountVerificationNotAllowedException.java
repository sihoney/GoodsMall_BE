package com.example.member.common.exception;

public class AccountVerificationNotAllowedException extends RuntimeException {

    public AccountVerificationNotAllowedException() {
        super("Account verification is not allowed.");
    }

    public AccountVerificationNotAllowedException(String message) {
        super(message);
    }
}
