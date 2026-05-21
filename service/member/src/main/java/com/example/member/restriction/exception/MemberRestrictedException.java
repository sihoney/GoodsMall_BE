package com.example.member.restriction.exception;

import java.time.LocalDateTime;

public class MemberRestrictedException extends RuntimeException {

    public MemberRestrictedException(LocalDateTime endAt) {
        super("회원은 " + endAt + "까지 이용이 제한됩니다.");
    }
}
