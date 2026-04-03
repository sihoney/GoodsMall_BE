package com.example.member.common.exception;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException() {
        super("Invalid email or password.");
    }
}
