package com.example.payment.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementCandidateExcludedEvent(
        UUID eventId,
        UUID refundId,
        UUID orderId,
        UUID escrowId,
        UUID orderItemId,
        UUID sellerMemberId,
        UUID buyerMemberId,
        Long refundAmount,
        LocalDateTime occurredAt
) {
}
