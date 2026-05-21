package com.example.member.auth.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("유효하지 않거나 만료된 비밀번호 재설정 링크입니다.");
    }
}
