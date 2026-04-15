package com.example.member.common.exception;

public class EmailVerificationRequiredException extends IllegalStateException {

    public EmailVerificationRequiredException() {
        super("Email verification is required.");
    }
}
