package com.example.member.common.exception;

public class EmailVerificationNotAllowedException extends RuntimeException {

    public EmailVerificationNotAllowedException() {
        super("이메일 인증을 진행할 수 없습니다.");
    }

    public EmailVerificationNotAllowedException(String message) {
        super(message);
    }
}
