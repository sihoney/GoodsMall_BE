package com.example.settlement.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementRefundExclusionCommand(
        UUID eventId,
        UUID refundId,
        UUID orderId,
        UUID escrowId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        Long refundAmount,
        LocalDateTime occurredAt
) {
}
