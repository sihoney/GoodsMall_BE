package com.example.member.common.exception;

public class InvalidEmailVerificationTokenException extends RuntimeException {

    public InvalidEmailVerificationTokenException() {
        super("이메일 인증 토큰이 올바르지 않습니다.");
    }
}
