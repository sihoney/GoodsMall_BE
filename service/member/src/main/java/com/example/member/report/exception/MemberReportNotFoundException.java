package com.example.member.report.exception;

public class MemberReportNotFoundException extends RuntimeException {

    public MemberReportNotFoundException() {
        super("회원 신고를 찾을 수 없습니다.");
    }
}
