package com.example.member.common.exception;

public class LastLoginMethodRemovalNotAllowedException extends RuntimeException {

    public LastLoginMethodRemovalNotAllowedException() {
        super("마지막 로그인 수단은 해제할 수 없습니다.");
    }
}
