package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record PaymentCompletedMessage(
        UUID paymentId,
        UUID orderId,
        Long amount
) {
}
