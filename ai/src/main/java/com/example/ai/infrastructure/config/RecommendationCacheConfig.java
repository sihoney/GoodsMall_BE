package com.example.ai.infrastructure.config;

import com.example.ai.application.service.RecommendationLimitPolicy;
import java.lang.reflect.Method;
import java.time.Duration;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RecommendationCacheConfig {

    public static final String RECOMMENDATION_CACHE_NAME = "ai:recommendation:products";

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            @Value("${ai.cache.recommendation-ttl-seconds:600}") long recommendationTtlSeconds
    ) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(recommendationTtlSeconds))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                ));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration(RECOMMENDATION_CACHE_NAME, cacheConfiguration)
                .transactionAware()
                .build();
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
