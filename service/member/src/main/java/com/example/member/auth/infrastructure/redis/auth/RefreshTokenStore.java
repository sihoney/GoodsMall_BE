package com.example.member.auth.infrastructure.redis.auth;

import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RefreshTokenStore {

    void createSession(
            UUID memberId,
            UUID sessionId,
            String refreshTokenId,
            Duration ttl,
            AuthSessionMetadata metadata
    );

    Optional<AuthSession> findBySessionId(UUID sessionId);

    List<AuthSession> findSessionsByMemberId(UUID memberId);

    void updateRefreshTokenId(
        UUID sessionId, 
        String refreshTokenId, 
        Duration ttl, 
        AuthSessionMetadata metadata
    );

    void deleteSession(UUID memberId, UUID sessionId);

    void deleteAllSessions(UUID memberId);

    Set<UUID> findSessionIdsByMemberId(UUID memberId);
}
