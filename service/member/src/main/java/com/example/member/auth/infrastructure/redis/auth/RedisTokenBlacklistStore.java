package com.example.member.auth.infrastructure.redis.auth;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistStore implements TokenBlacklistStore {

    private static final String ACCESS_TOKEN_PREFIX = "auth:blacklist:access:";
    private static final String SESSION_PREFIX = "auth:blacklist:session:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklistAccessToken(String accessTokenId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildAccessTokenKey(accessTokenId), "1", ttl);
    }

    @Override
    public void blacklistSession(UUID sessionId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildSessionKey(sessionId), "1", ttl);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String accessTokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildAccessTokenKey(accessTokenId)));
    }

    @Override
    public boolean isSessionBlacklisted(UUID sessionId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildSessionKey(sessionId)));
    }

    private String buildAccessTokenKey(String accessTokenId) {
        return ACCESS_TOKEN_PREFIX + accessTokenId;
    }

    private String buildSessionKey(UUID sessionId) {
        return SESSION_PREFIX + sessionId;
    }
}
