package com.example.member.common.exception;

public class SelfReportNotAllowedException extends RuntimeException {

    public SelfReportNotAllowedException() {
        super("자기 자신은 신고할 수 없습니다.");
    }
}
