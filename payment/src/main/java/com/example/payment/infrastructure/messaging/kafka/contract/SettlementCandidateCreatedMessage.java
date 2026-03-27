package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementCandidateCreatedMessage(
        UUID eventId,
        UUID orderId,
        UUID escrowId,
        UUID sellerMemberId,
        Long grossAmount,
        LocalDateTime releasedAt,
        ConfirmationType confirmationType,
        LocalDateTime occurredAt
) {
}
