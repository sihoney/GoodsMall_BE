package com.todaylunch.gateway.security;

public enum AuthErrorCode {
    TOKEN_EXPIRED("액세스 토큰이 만료되었습니다."),
    INVALID_TOKEN("유효하지 않은 액세스 토큰입니다."),
    UNAUTHORIZED("Authorization 헤더가 없거나 형식이 올바르지 않습니다."),
    ACCESS_DENIED("해당 리소스에 접근할 권한이 없습니다.");

    private final String message;

    AuthErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
