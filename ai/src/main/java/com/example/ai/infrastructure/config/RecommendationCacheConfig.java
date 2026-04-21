package com.example.ai.infrastructure.config;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.application.service.RecommendationLimitPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RecommendationCacheConfig {

    public static final String RECOMMENDATION_CACHE_NAME = "ai:recommendation:products";

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper,
            @Value("${ai.cache.recommendation-ttl-seconds:600}") long recommendationTtlSeconds
    ) {
        RedisSerializer<List<RecommendedProductResult>> valueSerializer =
                new RecommendationResultListRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(recommendationTtlSeconds))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration(RECOMMENDATION_CACHE_NAME, cacheConfiguration)
                .transactionAware()
                .build();
    }

    private static class RecommendationResultListRedisSerializer
            implements RedisSerializer<List<RecommendedProductResult>> {

        private final ObjectMapper objectMapper;
        private final JavaType valueType;

        private RecommendationResultListRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            this.valueType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, RecommendedProductResult.class);
        }

        @Override
        public byte[] serialize(List<RecommendedProductResult> value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (JsonProcessingException e) {
                throw new SerializationException("추천 결과 캐시 직렬화에 실패했습니다.", e);
            }
        }

        @Override
        public List<RecommendedProductResult> deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, valueType);
            } catch (IOException e) {
                String cachedValue = new String(bytes, StandardCharsets.UTF_8);
                throw new SerializationException("추천 결과 캐시 역직렬화에 실패했습니다. value=" + cachedValue, e);
            }
        }
    }

    @Bean("recommendationCacheKeyGenerator")
    public KeyGenerator recommendationCacheKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public @NonNull Object generate(Object target, @NonNull Method method, Object @NonNull ... params) {
                UUID productId = (UUID) params[0];
                return productId + ":" + RecommendationLimitPolicy.RELATED_TOP_LIMIT;
            }
        };
    }
}
