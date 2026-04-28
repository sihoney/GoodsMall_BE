package com.example.member.infrastructure.redis.auth;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RefreshTokenStore {

    void createSession(UUID memberId, UUID sessionId, String refreshTokenId, Duration ttl);

    Optional<AuthSession> findBySessionId(UUID sessionId);

    void updateRefreshTokenId(UUID sessionId, String refreshTokenId, Duration ttl);

    void deleteSession(UUID memberId, UUID sessionId);

    void deleteAllSessions(UUID memberId);

    Set<UUID> findSessionIdsByMemberId(UUID memberId);
}
