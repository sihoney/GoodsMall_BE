package com.example.order.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


@Configuration
public class RedisConfig {

    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper objectMapper
    ) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public ApplicationRunner redisServerConfig(RedisConnectionFactory connectionFactory) {
        return args -> {
            try (RedisConnection connection = connectionFactory.getConnection()) {
                connection.serverCommands().setConfig("maxmemory", "512mb");
                connection.serverCommands().setConfig("maxmemory-policy", "allkeys-lru");
            }
        };
    }
}
