package com.example.ai.infrastructure.embedding;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEventIdempotencyRepository implements EventIdempotencyRepository {

    private static final String PROCESSED_VALUE = "processed";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean reserve(String key, Duration ttl) {
        try {
            Boolean reserved = redisTemplate.opsForValue().setIfAbsent(key, PROCESSED_VALUE, ttl);
            return Boolean.TRUE.equals(reserved);
        } catch (RuntimeException e) {
            // Redis 장애 시 전체 consumer 중단보다 중복 허용을 선택한다.
            log.warn(
                    "Idempotency reserve failed. Continue processing without Redis protection and allow possible duplicates. key={}",
                    key,
                    e
            );
            return true;
        }
    }

    @Override
    public void release(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException e) {
            log.warn("Idempotency key release failed. key={}", key, e);
        }
    }
}

