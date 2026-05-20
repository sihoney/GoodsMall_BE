package com.example.settlement.infrastructure.messaging.kafka.contract;

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
        String confirmationType,
        Instant occurredAt
) {
}
