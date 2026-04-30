package com.example.ai.infrastructure.recommendation;

import com.example.ai.application.dto.RecommendedProductResult;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRecommendationRedisRepository {

    private static final String KEY_PREFIX = "user:recommendations:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.cache.user-recommendation-ttl-seconds:3600}")
    private long ttlSeconds;

    public void save(UUID memberId, List<RecommendedProductResult> results) {
        String key = KEY_PREFIX + memberId;
        try {
            String json = objectMapper.writeValueAsString(results);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (JacksonException e) {
            log.warn("사용자 추천 결과 저장 실패 memberId={}", memberId, e);
        }
    }

    public List<RecommendedProductResult> findByMemberId(UUID memberId) {
        String key = KEY_PREFIX + memberId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return List.of();
        }
        try {
            JavaType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RecommendedProductResult.class);
            return objectMapper.readValue(json, listType);
        } catch (JacksonException e) {
            log.warn("사용자 추천 결과 역직렬화 실패 memberId={}", memberId, e);
            return List.of();
        }
    }
}
