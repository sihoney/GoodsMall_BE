package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisAccountVerificationSessionStore implements AccountVerificationSessionStore {

    private static final String SESSION_KEY_PREFIX = "account-verification:session:";
    private static final String MEMBER_CURRENT_KEY_PREFIX = "account-verification:member:";
    private static final String MEMBER_CURRENT_KEY_SUFFIX = ":current";
    private static final String LOCK_KEY_PREFIX = "account-verification:lock:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveSession(AccountVerificationSession session, Duration ttl) {
        String key = buildSessionKey(session.getSessionId());
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        for (Map.Entry<String, String> entry : session.toMap().entrySet()) {
            hashOperations.put(key, entry.getKey(), entry.getValue());
        }
        if (ttl.isZero() || ttl.isNegative()) {
            stringRedisTemplate.delete(key);
            return;
        }
        stringRedisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<AccountVerificationSession> findSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AccountVerificationSession.fromMap(entries));
    }

    @Override
    public Optional<String> findCurrentSessionId(UUID memberId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(buildMemberCurrentKey(memberId)));
    }

    @Override
    public void saveCurrentSession(UUID memberId, String sessionId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildMemberCurrentKey(memberId), sessionId, ttl);
    }

    @Override
    public void deleteSession(String sessionId) {
        stringRedisTemplate.delete(buildSessionKey(sessionId));
        stringRedisTemplate.delete(buildLockKey(sessionId));
    }

    @Override
    public void deleteCurrentSession(UUID memberId) {
        stringRedisTemplate.delete(buildMemberCurrentKey(memberId));
    }

    @Override
    public boolean acquireLock(String sessionId, Duration ttl) {
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(buildLockKey(sessionId), "1", ttl);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public void releaseLock(String sessionId) {
        stringRedisTemplate.delete(buildLockKey(sessionId));
    }

    private String buildSessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String buildMemberCurrentKey(UUID memberId) {
        return MEMBER_CURRENT_KEY_PREFIX + memberId + MEMBER_CURRENT_KEY_SUFFIX;
    }

    private String buildLockKey(String sessionId) {
        return LOCK_KEY_PREFIX + sessionId;
    }
}
