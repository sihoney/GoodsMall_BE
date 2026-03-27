package com.todaylunch.common.security.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid token.");
    }
}
