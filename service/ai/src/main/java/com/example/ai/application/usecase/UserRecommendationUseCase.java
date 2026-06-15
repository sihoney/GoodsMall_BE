package com.example.ai.application.usecase;

import com.example.ai.application.dto.RecommendedProductResult;
import java.util.List;
import java.util.UUID;

public interface UserRecommendationUseCase {

    void updateRecommendationsForUser(UUID memberId, UUID productId);

    List<RecommendedProductResult> getRecommendationsForUser(UUID memberId);
}
