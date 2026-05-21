package com.example.member.verification.exception;

public class EmailVerificationRequiredException extends IllegalStateException {

    public EmailVerificationRequiredException() {
        super("이메일 인증이 필요합니다.");
    }
}
