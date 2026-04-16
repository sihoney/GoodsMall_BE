package com.example.ai.presentation.dto.response;

import com.example.ai.application.dto.EmbeddingAdminResult;

public record EmbeddingAdminResponse(
        int processedCount,
        int successCount,
        int skippedCount,
        int failedCount
) {

    public static EmbeddingAdminResponse from(EmbeddingAdminResult result) {
        return new EmbeddingAdminResponse(
                result.processedCount(),
                result.successCount(),
                result.skippedCount(),
                result.failedCount()
        );
    }
}

