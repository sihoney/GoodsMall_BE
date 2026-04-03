package com.example.settlement.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

public record SettlementCandidateCreatedMessage(
        UUID eventId,
        UUID orderId,
        UUID escrowId,
        UUID sellerMemberId,
        Long grossAmount,
        Instant releasedAt,
        String confirmationType,
        Instant occurredAt
) {
}
