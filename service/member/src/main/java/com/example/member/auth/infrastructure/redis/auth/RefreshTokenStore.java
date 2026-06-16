package com.example.member.auth.infrastructure.redis.auth;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RefreshTokenStore {

    void saveSession(AuthSession session, Duration ttl);

    Optional<AuthSession> findBySessionId(UUID sessionId);

    List<AuthSession> findSessionsByMemberId(UUID memberId);

    void deleteSession(UUID memberId, UUID sessionId);

    void deleteAllSessions(UUID memberId);

    Set<UUID> findSessionIdsByMemberId(UUID memberId);
}
