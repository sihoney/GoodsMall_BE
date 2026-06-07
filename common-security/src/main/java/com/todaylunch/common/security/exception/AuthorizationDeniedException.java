package com.todaylunch.common.security.exception;

// TODO: remove after all services migrate to SecurityException handlers.
@Deprecated
public class AuthorizationDeniedException extends SecurityException {

    public AuthorizationDeniedException() {
        this(SecurityErrorCode.AUTHORIZATION_DENIED);
    }

    public AuthorizationDeniedException(String message) {
        this(SecurityErrorCode.AUTHORIZATION_DENIED, message);
    }

    public AuthorizationDeniedException(SecurityErrorCode errorCode) {
        super(errorCode);
    }

    public AuthorizationDeniedException(SecurityErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
