package com.example.order.infrastructure.redis;

import com.example.order.application.port.CachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void registerExpire(UUID deliveryId) {
        redisTemplate.opsForValue().set(
                "delivery:expire:" + deliveryId,
                deliveryId.toString(),
                Duration.ofSeconds(10)
        );
    }
}
