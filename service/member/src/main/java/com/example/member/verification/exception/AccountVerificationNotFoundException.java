package com.example.member.verification.exception;

public class AccountVerificationNotFoundException extends RuntimeException {

    public AccountVerificationNotFoundException() {
        super("계좌 인증 세션을 찾을 수 없습니다.");
    }

    public AccountVerificationNotFoundException(String message) {
        super(message);
    }
}
