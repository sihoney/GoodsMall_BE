package com.example.member.common.exception;

public class InvalidEmailVerificationAutoLoginTokenException extends RuntimeException {

    public InvalidEmailVerificationAutoLoginTokenException() {
        super("이메일 인증 자동 로그인 토큰이 유효하지 않거나 만료되었습니다.");
    }
}
