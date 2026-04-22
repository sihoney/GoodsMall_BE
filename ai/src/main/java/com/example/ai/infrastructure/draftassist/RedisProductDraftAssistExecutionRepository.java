package com.example.ai.infrastructure.draftassist;

import com.example.ai.application.dto.ProductDraftAssistResult;
import com.example.ai.domain.service.ProductDraftAssistExecutionRepository;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisProductDraftAssistExecutionRepository implements ProductDraftAssistExecutionRepository {

    private static final String LOCK_PREFIX = "ai:draft-assist:lock:";
    private static final String RESULT_PREFIX = "ai:draft-assist:result:";
    private static final String LOCK_VALUE = "locked";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductDraftAssistResult> findCachedResult(String fingerprint) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(buildResultKey(fingerprint));
            if (cachedValue == null || cachedValue.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cachedValue, ProductDraftAssistResult.class));
        } catch (Exception e) {
            log.warn("Draft assist result cache lookup failed. fingerprint={}", fingerprint, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean tryLock(String fingerprint, Duration ttl) {
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(buildLockKey(fingerprint), LOCK_VALUE, ttl);
            return Boolean.TRUE.equals(locked);
        } catch (RuntimeException e) {
            log.warn("Draft assist lock failed. Continue without Redis lock. fingerprint={}", fingerprint, e);
            return true;
        }
    }

    @Override
    public void cacheResult(String fingerprint, ProductDraftAssistResult result, Duration ttl) {
        try {
            String serialized = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(buildResultKey(fingerprint), serialized, ttl);
        } catch (Exception e) {
            log.warn("Draft assist result cache write failed. fingerprint={}", fingerprint, e);
        }
    }

    @Override
    public void unlock(String fingerprint) {
        try {
            redisTemplate.delete(buildLockKey(fingerprint));
        } catch (RuntimeException e) {
            log.warn("Draft assist unlock failed. fingerprint={}", fingerprint, e);
        }
    }

    private String buildLockKey(String fingerprint) {
        return LOCK_PREFIX + fingerprint;
    }

    private String buildResultKey(String fingerprint) {
        return RESULT_PREFIX + fingerprint;
    }
}
