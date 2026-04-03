package com.example.payment.application.dto;

import com.example.payment.domain.enumtype.EscrowStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseResult(
        UUID orderId,
        Long releasedAmount,
        EscrowStatus escrowStatus,
        LocalDateTime releasedAt
) {
}
