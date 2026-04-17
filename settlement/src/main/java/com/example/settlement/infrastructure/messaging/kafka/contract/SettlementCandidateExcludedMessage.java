package com.example.settlement.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

public record SettlementCandidateExcludedMessage(
        UUID eventId,
        UUID refundId,
        UUID orderId,
        UUID escrowId,
        UUID orderItemId,
        UUID sellerMemberId,
        UUID buyerMemberId,
        Long refundAmount,
        Instant occurredAt
) {
}
