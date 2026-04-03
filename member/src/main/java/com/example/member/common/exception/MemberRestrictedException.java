package com.example.member.common.exception;

import java.time.LocalDateTime;

public class MemberRestrictedException extends RuntimeException {

    public MemberRestrictedException(LocalDateTime endAt) {
        super("Member is restricted until " + endAt + ".");
    }
}
