package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
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
