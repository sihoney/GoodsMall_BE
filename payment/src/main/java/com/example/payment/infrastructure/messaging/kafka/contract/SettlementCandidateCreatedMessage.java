package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementCandidateCreatedMessage(
        UUID eventId,
        UUID orderId,
        UUID escrowId,
        UUID sellerMemberId,
        BigDecimal grossAmount,
        Instant releasedAt,
        ConfirmationType confirmationType,
        Instant occurredAt
) {
}
