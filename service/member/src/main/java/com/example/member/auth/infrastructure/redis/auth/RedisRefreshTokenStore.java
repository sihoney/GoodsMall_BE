package com.example.member.auth.infrastructure.redis.auth;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveSession(AuthSession session, Duration ttl) {
        String sessionKey = buildSessionKey(session.sessionId());
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();

        for (Map.Entry<String, String> entry : session.toMap().entrySet()) {
            hashOperations.put(sessionKey, entry.getKey(), entry.getValue());
        }
        stringRedisTemplate.expire(sessionKey, ttl);

        String memberSessionsKey = buildMemberSessionsKey(session.memberId());
        stringRedisTemplate.opsForSet().add(memberSessionsKey, session.sessionId().toString());
        stringRedisTemplate.expire(memberSessionsKey, ttl);
    }

    @Override
    public Optional<AuthSession> findBySessionId(UUID sessionId) {
        String sessionKey = buildSessionKey(sessionId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(sessionKey);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(AuthSession.fromMap(entries));
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
