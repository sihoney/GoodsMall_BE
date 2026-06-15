package com.todaylunch.common.security.exception;

public class SecurityException extends RuntimeException {

    private final SecurityErrorCode errorCode;

    public SecurityException(SecurityErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public SecurityException(SecurityErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SecurityException(SecurityErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
    }

    public SecurityException(SecurityErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public SecurityErrorCode getErrorCode() {
        return errorCode;
    }
}
