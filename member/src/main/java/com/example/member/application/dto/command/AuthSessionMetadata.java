package com.example.member.application.dto.command;

public record AuthSessionMetadata(
        String userAgent,
        String ipAddress
) {
    public static AuthSessionMetadata empty() {
        return new AuthSessionMetadata(null, null);
    }
}
