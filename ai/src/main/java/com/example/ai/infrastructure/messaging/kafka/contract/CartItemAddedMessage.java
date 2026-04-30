package com.example.ai.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record CartItemAddedMessage(
        UUID memberId,
        UUID productId
) {
}
