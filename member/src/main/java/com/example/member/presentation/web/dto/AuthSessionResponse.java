package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.AuthSessionResult;
import java.time.Instant;
import java.util.UUID;

public record AuthSessionResponse(
        UUID sessionId,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant lastRefreshedAt,
        String userAgent,
        String ipAddress,
        boolean current
) {
    public static AuthSessionResponse from(AuthSessionResult result) {
        return new AuthSessionResponse(
                result.sessionId(),
                result.createdAt(),
                result.lastAccessedAt(),
                result.lastRefreshedAt(),
                result.userAgent(),
                result.ipAddress(),
                result.current()
        );
    }
}
