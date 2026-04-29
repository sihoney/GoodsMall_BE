package com.example.member.infrastructure.redis.auth;

import com.example.member.application.dto.command.AuthSessionMetadata;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String MEMBER_SESSIONS_KEY_PREFIX = "auth:member-sessions:";
    private static final String MEMBER_ID_FIELD = "memberId";
    private static final String SESSION_ID_FIELD = "sessionId";
    private static final String REFRESH_TOKEN_ID_FIELD = "refreshTokenId";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String LAST_ACCESSED_AT_FIELD = "lastAccessedAt";
    private static final String LAST_REFRESHED_AT_FIELD = "lastRefreshedAt";
    private static final String USER_AGENT_FIELD = "userAgent";
    private static final String IP_ADDRESS_FIELD = "ipAddress";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void createSession(
            UUID memberId,
            UUID sessionId,
            String refreshTokenId,
            Duration ttl,
            AuthSessionMetadata metadata
    ) {
        String sessionKey = buildSessionKey(sessionId);
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        Instant now = Instant.now();
        AuthSessionMetadata normalizedMetadata = normalizeMetadata(metadata);

        hashOperations.put(sessionKey, MEMBER_ID_FIELD, memberId.toString());
        hashOperations.put(sessionKey, SESSION_ID_FIELD, sessionId.toString());
        hashOperations.put(sessionKey, REFRESH_TOKEN_ID_FIELD, refreshTokenId);
        hashOperations.put(sessionKey, CREATED_AT_FIELD, now.toString());
        hashOperations.put(sessionKey, LAST_ACCESSED_AT_FIELD, now.toString());
        hashOperations.put(sessionKey, LAST_REFRESHED_AT_FIELD, now.toString());
        hashOperations.put(sessionKey, USER_AGENT_FIELD, normalizedMetadata.userAgent());
        hashOperations.put(sessionKey, IP_ADDRESS_FIELD, normalizedMetadata.ipAddress());
        stringRedisTemplate.expire(sessionKey, ttl);

        String memberSessionsKey = buildMemberSessionsKey(memberId);
        stringRedisTemplate.opsForSet().add(memberSessionsKey, sessionId.toString());
        stringRedisTemplate.expire(memberSessionsKey, ttl);
    }

    @Override
    public Optional<AuthSession> findBySessionId(UUID sessionId) {
        String sessionKey = buildSessionKey(sessionId);
        String memberId = (String) stringRedisTemplate.opsForHash().get(sessionKey, MEMBER_ID_FIELD);
        String storedSessionId = (String) stringRedisTemplate.opsForHash().get(sessionKey, SESSION_ID_FIELD);
        String refreshTokenId = (String) stringRedisTemplate.opsForHash().get(sessionKey, REFRESH_TOKEN_ID_FIELD);
        String createdAt = (String) stringRedisTemplate.opsForHash().get(sessionKey, CREATED_AT_FIELD);
        String lastAccessedAt = (String) stringRedisTemplate.opsForHash().get(sessionKey, LAST_ACCESSED_AT_FIELD);
        String lastRefreshedAt = (String) stringRedisTemplate.opsForHash().get(sessionKey, LAST_REFRESHED_AT_FIELD);
        String userAgent = (String) stringRedisTemplate.opsForHash().get(sessionKey, USER_AGENT_FIELD);
        String ipAddress = (String) stringRedisTemplate.opsForHash().get(sessionKey, IP_ADDRESS_FIELD);

        if (memberId == null
                || storedSessionId == null
                || refreshTokenId == null
                || createdAt == null
                || lastAccessedAt == null
                || lastRefreshedAt == null) {
            return Optional.empty();
        }

        return Optional.of(new AuthSession(
                UUID.fromString(memberId),
                UUID.fromString(storedSessionId),
                refreshTokenId,
                Instant.parse(createdAt),
                Instant.parse(lastAccessedAt),
                Instant.parse(lastRefreshedAt),
                emptyToNull(userAgent),
                emptyToNull(ipAddress)
        ));
    }

    @Override
    public List<AuthSession> findSessionsByMemberId(UUID memberId) {
        Set<UUID> sessionIds = findSessionIdsByMemberId(memberId);
        if (sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AuthSession> sessions = new ArrayList<>();
        for (UUID sessionId : sessionIds) {
            findBySessionId(sessionId).ifPresent(sessions::add);
        }
        return sessions;
    }

    @Override
    public void updateRefreshTokenId(
        UUID sessionId, 
        String refreshTokenId, 
        Duration ttl, 
        AuthSessionMetadata metadata
    ) {
        Optional<AuthSession> currentSession = findBySessionId(sessionId);
        if (currentSession.isEmpty()) {
            return;
        }

        AuthSession authSession = currentSession.get();
        String sessionKey = buildSessionKey(sessionId);
        Instant now = Instant.now();
        AuthSessionMetadata normalizedMetadata = normalizeMetadata(metadata);

        stringRedisTemplate.opsForHash().put(sessionKey, REFRESH_TOKEN_ID_FIELD, refreshTokenId);
        stringRedisTemplate.opsForHash().put(sessionKey, LAST_ACCESSED_AT_FIELD, now.toString());
        stringRedisTemplate.opsForHash().put(sessionKey, LAST_REFRESHED_AT_FIELD, now.toString());
        stringRedisTemplate.opsForHash().put(sessionKey, USER_AGENT_FIELD, normalizedMetadata.userAgent());
        stringRedisTemplate.opsForHash().put(sessionKey, IP_ADDRESS_FIELD, normalizedMetadata.ipAddress());
        stringRedisTemplate.expire(sessionKey, ttl);
        stringRedisTemplate.expire(buildMemberSessionsKey(authSession.memberId()), ttl);
    }

    @Override
    public void deleteSession(UUID memberId, UUID sessionId) {
        stringRedisTemplate.delete(buildSessionKey(sessionId));
        stringRedisTemplate.opsForSet().remove(buildMemberSessionsKey(memberId), sessionId.toString());
    }

    @Override
    public void deleteAllSessions(UUID memberId) {
        Set<UUID> sessionIds = findSessionIdsByMemberId(memberId);
        if (sessionIds.isEmpty()) {
            stringRedisTemplate.delete(buildMemberSessionsKey(memberId));
            return;
        }

        for (UUID sessionId : sessionIds) {
            stringRedisTemplate.delete(buildSessionKey(sessionId));
        }
        stringRedisTemplate.delete(buildMemberSessionsKey(memberId));
    }

    @Override
    public Set<UUID> findSessionIdsByMemberId(UUID memberId) {
        Set<String> sessionIdValues = stringRedisTemplate.opsForSet().members(buildMemberSessionsKey(memberId));
        if (sessionIdValues == null || sessionIdValues.isEmpty()) {
            return Collections.emptySet();
        }

        Set<UUID> sessionIds = new HashSet<>();
        for (String sessionIdValue : sessionIdValues) {
            sessionIds.add(UUID.fromString(sessionIdValue));
        }
        return sessionIds;
    }

    private String buildSessionKey(UUID sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String buildMemberSessionsKey(UUID memberId) {
        return MEMBER_SESSIONS_KEY_PREFIX + memberId;
    }

    private AuthSessionMetadata normalizeMetadata(AuthSessionMetadata metadata) {
        if (metadata == null) {
            return new AuthSessionMetadata("", "");
        }

        return new AuthSessionMetadata(
                normalizeValue(metadata.userAgent(), 512),
                normalizeValue(metadata.ipAddress(), 128)
        );
    }

    private String normalizeValue(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
