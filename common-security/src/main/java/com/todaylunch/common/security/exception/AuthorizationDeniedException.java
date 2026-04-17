package com.todaylunch.common.security.exception;

public class AuthorizationDeniedException extends RuntimeException {

    public AuthorizationDeniedException() {
        super("Access denied.");
    }

    public AuthorizationDeniedException(String message) {
        super(message);
    }
}
