package com.example.settlement.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementItemCreateCommand(
        UUID orderId,
        UUID escrowId,
        UUID sellerId,
        Long grossAmount,
        LocalDateTime releasedAt
) {
}
