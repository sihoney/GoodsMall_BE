package com.example.order.application.port;

import java.util.UUID;

public interface CachePort {
    void registerExpire(UUID deliveryId);
}
