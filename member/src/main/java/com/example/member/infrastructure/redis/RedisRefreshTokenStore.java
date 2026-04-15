package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
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

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void createSession(UUID memberId, UUID sessionId, String refreshTokenId, Duration ttl) {
        String sessionKey = buildSessionKey(sessionId);
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        hashOperations.put(sessionKey, MEMBER_ID_FIELD, memberId.toString());
        hashOperations.put(sessionKey, SESSION_ID_FIELD, sessionId.toString());
        hashOperations.put(sessionKey, REFRESH_TOKEN_ID_FIELD, refreshTokenId);
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

        if (memberId == null || storedSessionId == null || refreshTokenId == null) {
            return Optional.empty();
        }

        return Optional.of(new AuthSession(
                UUID.fromString(memberId),
                UUID.fromString(storedSessionId),
                refreshTokenId
        ));
    }

    @Override
    public void updateRefreshTokenId(UUID sessionId, String refreshTokenId, Duration ttl) {
        Optional<AuthSession> currentSession = findBySessionId(sessionId);
        if (currentSession.isEmpty()) {
            return;
        }

        AuthSession authSession = currentSession.get();
        String sessionKey = buildSessionKey(sessionId);
        stringRedisTemplate.opsForHash().put(sessionKey, REFRESH_TOKEN_ID_FIELD, refreshTokenId);
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
}
