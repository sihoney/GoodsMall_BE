package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EscrowReleaseScheduleRequest(
        @NotNull(message = "deliveredAt is required.")
        LocalDateTime deliveredAt
) {
}
