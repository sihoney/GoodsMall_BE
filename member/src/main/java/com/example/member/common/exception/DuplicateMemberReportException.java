package com.example.member.common.exception;

public class DuplicateMemberReportException extends RuntimeException {

    public DuplicateMemberReportException() {
        super("A pending report for this member already exists from the same reporter.");
    }
}
