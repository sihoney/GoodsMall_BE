package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(UUID memberId, String refreshToken, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildKey(memberId), refreshToken, ttl);
    }

    @Override
    public Optional<String> findByMemberId(UUID memberId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(buildKey(memberId)));
    }

    @Override
    public void delete(UUID memberId) {
        stringRedisTemplate.delete(buildKey(memberId));
    }

    private String buildKey(UUID memberId) {
        return KEY_PREFIX + memberId;
    }
}
