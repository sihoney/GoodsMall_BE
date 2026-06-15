package com.example.member.common.application.dto;

public record AuthSessionMetadata(
        String userAgent,
        String ipAddress
) {
    public static AuthSessionMetadata empty() {
        return new AuthSessionMetadata(null, null);
    }
}
