package com.example.member.auth.exception;

public class MemberOauthAccountNotFoundException extends RuntimeException {

    public MemberOauthAccountNotFoundException() {
        super("연동된 외부 계정을 찾을 수 없습니다.");
    }
}
