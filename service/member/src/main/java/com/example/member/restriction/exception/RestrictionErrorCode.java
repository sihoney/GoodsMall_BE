package com.example.member.restriction.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RestrictionErrorCode implements ErrorCode {

    MEMBER_RESTRICTED(HttpStatus.FORBIDDEN, "MEMBER_RESTRICTED", "회원 이용이 제한되었습니다."),
    DUPLICATE_ACTIVE_RESTRICTION(HttpStatus.CONFLICT, "DUPLICATE_ACTIVE_RESTRICTION", "해당 회원에게 같은 유형의 활성 제재가 이미 존재합니다."),
    MEMBER_RESTRICTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_RESTRICTION_NOT_FOUND", "회원 제재 내역을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    RestrictionErrorCode(HttpStatus status, String code, String message) {
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
