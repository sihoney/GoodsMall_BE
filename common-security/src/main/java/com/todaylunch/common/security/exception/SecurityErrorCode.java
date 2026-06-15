package com.todaylunch.common.security.exception;

import org.springframework.http.HttpStatus;

public enum SecurityErrorCode {
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_REQUIRED",
            "Authentication is required."
    ),
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "INVALID_TOKEN",
            "Invalid token."
    ),
    AUTHORIZATION_DENIED(
            HttpStatus.FORBIDDEN,
            "AUTHORIZATION_DENIED",
            "Access denied."
    ),
    INSUFFICIENT_ROLE(
            HttpStatus.FORBIDDEN,
            "INSUFFICIENT_ROLE",
            "Required role is missing."
    ),
    RESOURCE_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "RESOURCE_ACCESS_DENIED",
            "Resource owner or ADMIN role is required."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    SecurityErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
