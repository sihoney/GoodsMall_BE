package com.example.member.member.exception;

public class MemberSuspendedException extends IllegalStateException {

    public MemberSuspendedException() {
        super("정지된 회원 계정입니다.");
    }
}
