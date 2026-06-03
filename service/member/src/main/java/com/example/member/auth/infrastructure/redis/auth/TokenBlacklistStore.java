package com.example.member.auth.infrastructure.redis.auth;

import java.time.Duration;
import java.util.UUID;

public interface TokenBlacklistStore {

    void blacklistAccessToken(String accessTokenId, Duration ttl);

    // TODO: 현재 검증 경로에서 사용되지 않는다. auth:session:{sessionId} whitelist 검증으로 대체 후 제거한다.
    void blacklistSession(UUID sessionId, Duration ttl);

    boolean isAccessTokenBlacklisted(String accessTokenId);

    // TODO: 현재 검증 경로에서 사용되지 않는다. auth:session:{sessionId} whitelist 검증으로 대체 후 제거한다.
    boolean isSessionBlacklisted(UUID sessionId);
}
