package com.example.member.verification.exception;

public class ExpiredAccountVerificationException extends RuntimeException {

    public ExpiredAccountVerificationException() {
        super("계좌 인증 세션이 만료되었습니다.");
    }

    public ExpiredAccountVerificationException(String message) {
        super(message);
    }
}
