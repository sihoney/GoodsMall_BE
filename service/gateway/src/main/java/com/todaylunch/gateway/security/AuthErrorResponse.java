package com.todaylunch.gateway.security;

public record AuthErrorResponse(
        String code,
        String message
) {
    public static AuthErrorResponse from(AuthErrorCode errorCode) {
        return new AuthErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}
