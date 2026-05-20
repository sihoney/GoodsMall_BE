package com.example.notification.infrastructure.messaging.kafka.contract;

import java.util.UUID;

public record PaymentCancelledMessage(
        UUID paymentId,
        UUID orderId,
        Long refundAmount
) {
}
