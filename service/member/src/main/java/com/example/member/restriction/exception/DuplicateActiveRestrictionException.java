package com.example.member.restriction.exception;

public class DuplicateActiveRestrictionException extends RuntimeException {

    public DuplicateActiveRestrictionException() {
        super("해당 회원에게 같은 유형의 활성 제재가 이미 존재합니다.");
    }
}
