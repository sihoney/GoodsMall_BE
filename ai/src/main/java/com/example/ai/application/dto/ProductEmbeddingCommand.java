package com.example.ai.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductEmbeddingCommand(
        UUID productId,
        String productName,
        String categoryName,
        String description,
        LocalDateTime sourceUpdatedAt
) {
}
