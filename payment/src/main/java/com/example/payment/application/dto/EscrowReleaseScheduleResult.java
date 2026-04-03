package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseScheduleResult(
        UUID orderId,
        LocalDateTime deliveredAt,
        LocalDateTime releaseAt
) {
}
