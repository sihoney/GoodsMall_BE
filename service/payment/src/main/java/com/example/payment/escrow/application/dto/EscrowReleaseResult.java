package com.example.payment.escrow.application.dto;

import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseResult(
        UUID orderId,
        BigDecimal releasedAmount,
        EscrowStatus escrowStatus,
        LocalDateTime releasedAt
) {
}
