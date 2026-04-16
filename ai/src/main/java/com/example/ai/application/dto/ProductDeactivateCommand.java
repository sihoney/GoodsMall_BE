package com.example.ai.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDeactivateCommand(
        UUID productId,
        LocalDateTime sourceUpdatedAt
) {
}

