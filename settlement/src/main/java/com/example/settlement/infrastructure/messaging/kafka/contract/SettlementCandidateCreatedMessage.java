package com.example.settlement.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementCandidateCreatedMessage(
        UUID eventId,
        UUID orderId,
        UUID escrowId,
        UUID sellerMemberId,
        Long grossAmount,
        LocalDateTime releasedAt,
        String confirmationType,
        LocalDateTime occurredAt
) {
}
