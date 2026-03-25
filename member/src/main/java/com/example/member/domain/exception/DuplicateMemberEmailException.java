package com.example.member.domain.exception;

public class DuplicateMemberEmailException extends RuntimeException {

    public DuplicateMemberEmailException() {
        super("이미 사용 중인 이메일입니다.");
    }
}
