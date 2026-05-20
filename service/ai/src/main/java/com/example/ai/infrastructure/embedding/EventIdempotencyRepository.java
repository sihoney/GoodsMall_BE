package com.example.ai.infrastructure.embedding;

import java.time.Duration;

public interface EventIdempotencyRepository {

    boolean reserve(String key, Duration ttl);

    void release(String key);
}

