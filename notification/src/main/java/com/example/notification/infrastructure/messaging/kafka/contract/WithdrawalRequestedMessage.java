package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record WithdrawalRequestedMessage(
        UUID withdrawalId,
        UUID sellerId,
        Long amount
) {
}
