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

/*
 * Redis schema
 *
 * [1] 인증 세션 본문
 * - key: auth:session:{sessionId}
 * - type: Hash
 * - ttl: refresh token 만료 시간
 * - value:
 *   - memberId: 회원 ID
 *   - sessionId: 인증 세션 ID
 *   - refreshTokenId: refresh token 식별자
 *   - createdAt: 세션 생성 시각
 *   - lastAccessedAt: 마지막 접근 시각
 *   - lastRefreshedAt: 마지막 refresh 시각
 *   - userAgent: 사용자 agent, 없으면 빈 문자열
 *   - ipAddress: IP 주소, 없으면 빈 문자열
 *
 * [2] 회원별 세션 목록
 * - key: auth:member-sessions:{memberId}
 * - type: Set
 * - ttl: refresh token 만료 시간
 * - value: sessionId 목록
 *
 * [3] 저장 예시
 * - auth:session:11111111-1111-1111-1111-111111111111
 *   memberId = 22222222-2222-2222-2222-222222222222
 *   sessionId = 11111111-1111-1111-1111-111111111111
 *   refreshTokenId = rt_abc123
 *   createdAt = 2026-06-16T14:30:00Z
 *   lastAccessedAt = 2026-06-16T14:30:00Z
 *   lastRefreshedAt = 2026-06-16T14:30:00Z
 *   userAgent = Mozilla/5.0
 *   ipAddress = 127.0.0.1
 *
 * - auth:member-sessions:22222222-2222-2222-2222-222222222222
 *   value = 11111111-1111-1111-1111-111111111111
 */
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
