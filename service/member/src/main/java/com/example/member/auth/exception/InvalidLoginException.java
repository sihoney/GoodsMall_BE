package com.example.member.auth.exception;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
