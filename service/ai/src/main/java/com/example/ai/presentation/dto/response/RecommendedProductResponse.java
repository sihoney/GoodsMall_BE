package com.example.ai.presentation.dto.response;

import com.example.ai.application.dto.RecommendedProductResult;
import java.util.UUID;

public record RecommendedProductResponse(
        UUID productId,
        double similarityScore
) {

    public static RecommendedProductResponse from(RecommendedProductResult result) {
        return new RecommendedProductResponse(result.productId(), result.similarityScore());
    }
}

