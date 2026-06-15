package com.example.member.auth.application.dto.result;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionResult(
        UUID sessionId,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant lastRefreshedAt,
        String userAgent,
        String ipAddress,
        boolean current
) {
}
