package com.example.member.common.exception;

public class SelfReportNotAllowedException extends RuntimeException {

    public SelfReportNotAllowedException() {
        super("You cannot report yourself.");
    }
}
