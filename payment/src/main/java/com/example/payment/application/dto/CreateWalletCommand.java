package com.example.payment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateWalletCommand(
        UUID memberId,
        LocalDateTime createdAt
) {
}
