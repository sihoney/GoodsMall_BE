package com.example.member.domain.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("Refresh token not found.");
    }
}
