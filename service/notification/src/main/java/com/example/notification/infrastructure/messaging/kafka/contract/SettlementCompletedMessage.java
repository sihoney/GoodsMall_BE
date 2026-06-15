package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record SettlementCompletedMessage(
        UUID settlementId,
        UUID sellerId,
        Long amount
) {
}
