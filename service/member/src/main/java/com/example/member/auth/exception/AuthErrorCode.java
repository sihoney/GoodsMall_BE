package com.example.member.auth.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PASSWORD_RESET_TOKEN(
            HttpStatus.BAD_REQUEST,
            "INVALID_PASSWORD_RESET_TOKEN",
            "유효하지 않거나 만료된 비밀번호 재설정 링크입니다."
    ),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    OAUTH_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "OAUTH_INVALID_REQUEST", "OAuth request is invalid."),
    OAUTH_INVALID_STATE(HttpStatus.BAD_REQUEST, "OAUTH_INVALID_STATE", "OAuth state is invalid or expired."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OAUTH_PROVIDER", "Unsupported OAuth provider.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
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