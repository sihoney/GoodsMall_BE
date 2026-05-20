package com.example.payment.common.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * payment媛 order???섎룎??二쇰뒗 二쇰Ц 寃곗젣 寃곌낵 Kafka 怨꾩빟 硫붿떆吏??
 */
public record OrderPaymentResultMessage(
        UUID eventId,
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal amount,
        OrderPaymentResultStatus status,
        OrderPaymentFailureReason reasonCode,
        Instant occurredAt
) {
}
