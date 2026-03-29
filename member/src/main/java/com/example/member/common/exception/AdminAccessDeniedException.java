package com.example.member.common.exception;

public class AdminAccessDeniedException extends RuntimeException {

    public AdminAccessDeniedException() {
        super("Admin access is required.");
    }
}
