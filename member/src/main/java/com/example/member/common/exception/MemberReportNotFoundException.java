package com.example.member.common.exception;

public class MemberReportNotFoundException extends RuntimeException {

    public MemberReportNotFoundException() {
        super("Member report was not found.");
    }
}
