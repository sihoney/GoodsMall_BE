package com.example.member.common.exception;

public class MemberWithdrawnException extends IllegalStateException {

    public MemberWithdrawnException() {
        super("탈퇴한 계정입니다. 같은 이메일로 다시 가입해 주세요.");
    }
}
