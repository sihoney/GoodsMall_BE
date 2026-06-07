package com.example.member.verification.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum VerificationErrorCode implements ErrorCode {
    EMAIL_VERIFICATION_REQUIRED(HttpStatus.FORBIDDEN, "EMAIL_VERIFICATION_REQUIRED", "이메일 인증이 필요합니다."),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_TOKEN_INVALID", "이메일 인증 토큰이 올바르지 않습니다."),
    EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID(
            HttpStatus.UNAUTHORIZED,
            "EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID",
            "이메일 인증 자동 로그인 토큰이 유효하지 않거나 만료되었습니다."
    ),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.GONE, "EMAIL_VERIFICATION_TOKEN_EXPIRED", "이메일 인증 토큰이 만료되었습니다."),
    EMAIL_VERIFICATION_NOT_ALLOWED(HttpStatus.CONFLICT, "EMAIL_VERIFICATION_NOT_ALLOWED", "이메일 인증을 진행할 수 없습니다."),
    EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_SEND_FAILED", "이메일 전송에 실패했습니다."),
    ACCOUNT_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_VERIFICATION_NOT_FOUND", "계좌 인증 세션을 찾을 수 없습니다."),
    ACCOUNT_VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "ACCOUNT_VERIFICATION_CODE_INVALID", "계좌 인증 코드가 올바르지 않습니다."),
    ACCOUNT_VERIFICATION_EXPIRED(HttpStatus.GONE, "ACCOUNT_VERIFICATION_EXPIRED", "계좌 인증 세션이 만료되었습니다."),
    ACCOUNT_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "ACCOUNT_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED",
            "계좌 인증 시도 횟수를 초과했습니다."
    ),
    ACCOUNT_VERIFICATION_RESEND_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "ACCOUNT_VERIFICATION_RESEND_LIMIT_EXCEEDED",
            "계좌 인증 재전송 횟수를 초과했습니다."
    ),
    ACCOUNT_VERIFICATION_NOT_ALLOWED(HttpStatus.CONFLICT, "ACCOUNT_VERIFICATION_NOT_ALLOWED", "계좌 인증을 진행할 수 없습니다."),
    INVALID_ACCOUNT_NUMBER(
            HttpStatus.BAD_REQUEST,
            "INVALID_ACCOUNT_NUMBER",
            "accountNumber는 숫자만 포함해야 하고 6자 이상 20자 이하여야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    VerificationErrorCode(HttpStatus status, String code, String message) {
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
