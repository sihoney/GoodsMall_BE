package com.example.member.auth.infrastructure.redis.oauth;

import java.time.Instant;

public record OAuthAuthorizeState(
        String state,
        Instant createdAt
) {
}
