package com.example.settlement.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementItemCreateCommand(
        UUID orderId,
        UUID escrowId,
        UUID sellerId,
        BigDecimal grossAmount,
        LocalDateTime releasedAt
) {
}
