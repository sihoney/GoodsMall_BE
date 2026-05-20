package com.example.member.auth.infrastructure.redis.auth;

import java.time.Duration;
import java.util.UUID;

public interface TokenBlacklistStore {

    void blacklistAccessToken(String accessTokenId, Duration ttl);

    void blacklistSession(UUID sessionId, Duration ttl);

    boolean isAccessTokenBlacklisted(String accessTokenId);

    boolean isSessionBlacklisted(UUID sessionId);
}
