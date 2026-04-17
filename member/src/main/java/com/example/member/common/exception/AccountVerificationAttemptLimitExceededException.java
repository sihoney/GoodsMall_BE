package com.example.member.common.exception;

public class AccountVerificationAttemptLimitExceededException extends RuntimeException {

    public AccountVerificationAttemptLimitExceededException() {
        super("Account verification attempt limit exceeded.");
    }

    public AccountVerificationAttemptLimitExceededException(String message) {
        super(message);
    }
}
