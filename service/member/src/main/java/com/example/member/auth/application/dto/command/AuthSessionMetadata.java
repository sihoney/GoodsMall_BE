package com.example.member.auth.application.dto.command;

public record AuthSessionMetadata(
        String userAgent,
        String ipAddress
) {
    public static AuthSessionMetadata empty() {
        return new AuthSessionMetadata(null, null);
    }
}
