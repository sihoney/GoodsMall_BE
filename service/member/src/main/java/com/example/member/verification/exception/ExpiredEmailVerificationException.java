package com.example.member.verification.exception;

public class ExpiredEmailVerificationException extends RuntimeException {

    public ExpiredEmailVerificationException() {
        super("이메일 인증 토큰이 만료되었습니다.");
    }
}
