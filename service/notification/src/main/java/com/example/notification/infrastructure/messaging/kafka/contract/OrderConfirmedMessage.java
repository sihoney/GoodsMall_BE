package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.List;
import java.util.UUID;

public record OrderConfirmedMessage(
        UUID orderId,
        List<UUID> orderItemIds,
        List<UUID> sellerIds
) {
}
