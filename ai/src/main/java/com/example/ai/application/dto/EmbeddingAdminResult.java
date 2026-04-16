package com.example.ai.application.dto;

public record EmbeddingAdminResult(
        int processedCount,
        int successCount,
        int skippedCount,
        int failedCount
) {
}

