package com.example.member.verification.exception;

public class AccountVerificationNotAllowedException extends RuntimeException {

    public AccountVerificationNotAllowedException() {
        super("계좌 인증을 진행할 수 없습니다.");
    }

    public AccountVerificationNotAllowedException(String message) {
        super(message);
    }
}
