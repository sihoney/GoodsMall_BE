package com.example.member.common.exception;

public class MemberRestrictionNotFoundException extends RuntimeException {

    public MemberRestrictionNotFoundException() {
        super("회원 제재 내역을 찾을 수 없습니다.");
    }
}
