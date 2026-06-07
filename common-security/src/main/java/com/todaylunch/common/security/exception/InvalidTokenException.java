package com.todaylunch.common.security.exception;

// TODO: remove after all services migrate to SecurityException handlers.
@Deprecated
public class InvalidTokenException extends SecurityException {

    public InvalidTokenException() {
        this(SecurityErrorCode.INVALID_TOKEN);
    }

    public InvalidTokenException(SecurityErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidTokenException(SecurityErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
