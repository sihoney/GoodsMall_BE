package com.example.member.common.exception;

public class AccountVerificationAttemptLimitExceededException extends RuntimeException {

    public AccountVerificationAttemptLimitExceededException() {
        super("계좌 인증 시도 횟수를 초과했습니다.");
    }

    public AccountVerificationAttemptLimitExceededException(String message) {
        super(message);
    }
}
