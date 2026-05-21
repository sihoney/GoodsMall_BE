package com.example.member.verification.exception;

public class AccountVerificationResendLimitExceededException extends RuntimeException {

    public AccountVerificationResendLimitExceededException() {
        super("계좌 인증 재전송 횟수를 초과했습니다.");
    }

    public AccountVerificationResendLimitExceededException(String message) {
        super(message);
    }
}
