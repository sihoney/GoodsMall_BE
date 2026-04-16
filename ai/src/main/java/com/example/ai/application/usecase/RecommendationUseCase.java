package com.example.ai.application.usecase;

import com.example.ai.application.dto.RecommendedProductResult;
import java.util.List;
import java.util.UUID;

public interface RecommendationUseCase {

    List<RecommendedProductResult> recommend(UUID productId);
}

