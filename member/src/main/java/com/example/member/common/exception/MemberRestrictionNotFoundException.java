package com.example.member.common.exception;

public class MemberRestrictionNotFoundException extends RuntimeException {

    public MemberRestrictionNotFoundException() {
        super("Member restriction was not found.");
    }
}
