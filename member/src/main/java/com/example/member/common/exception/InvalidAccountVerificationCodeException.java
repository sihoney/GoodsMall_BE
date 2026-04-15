package com.example.member.common.exception;

public class InvalidAccountVerificationCodeException extends RuntimeException {

    public InvalidAccountVerificationCodeException() {
        super("Account verification code is invalid.");
    }

    public InvalidAccountVerificationCodeException(String message) {
        super(message);
    }
}
