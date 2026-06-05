package com.example.member.report.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ReportErrorCode implements ErrorCode {

    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_REPORT_NOT_ALLOWED", "자기 자신은 신고할 수 없습니다."),
    DUPLICATE_MEMBER_REPORT(HttpStatus.CONFLICT, "DUPLICATE_MEMBER_REPORT", "같은 신고자에 대해 처리 대기 중인 회원 신고가 이미 존재합니다."),
    MEMBER_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_REPORT_NOT_FOUND", "회원 신고를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ReportErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
