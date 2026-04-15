package com.example.member.common.exception;

public class ExpiredAccountVerificationException extends RuntimeException {

    public ExpiredAccountVerificationException() {
        super("Account verification session has expired.");
    }

    public ExpiredAccountVerificationException(String message) {
        super(message);
    }
}
