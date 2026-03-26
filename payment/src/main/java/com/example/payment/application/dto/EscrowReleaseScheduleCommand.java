package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EscrowReleaseScheduleCommand(
        UUID orderId,
        LocalDateTime deliveredAt
) {
}
