package com.example.payment.application.event;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementCandidateCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID escrowId,
        UUID sellerMemberId,
        BigDecimal grossAmount,
        LocalDateTime releasedAt,
        ConfirmationType confirmationType,
        LocalDateTime occurredAt
) {
}
