package com.example.member.common.exception;

public class AccountVerificationResendLimitExceededException extends RuntimeException {

    public AccountVerificationResendLimitExceededException() {
        super("Account verification resend limit exceeded.");
    }

    public AccountVerificationResendLimitExceededException(String message) {
        super(message);
    }
}
