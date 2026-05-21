package com.example.member.verification.exception;

public class InvalidAccountVerificationCodeException extends RuntimeException {

    public InvalidAccountVerificationCodeException() {
        super("계좌 인증 코드가 올바르지 않습니다.");
    }

    public InvalidAccountVerificationCodeException(String message) {
        super(message);
    }
}
