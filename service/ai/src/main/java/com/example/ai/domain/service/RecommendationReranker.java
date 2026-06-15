package com.example.ai.domain.service;

import com.example.ai.application.dto.RecommendedProductResult;
import java.util.List;
import java.util.UUID;

public interface RecommendationReranker {

    List<UUID> rerank(UUID baseProductId, List<RecommendedProductResult> candidates, int selectCount);
}
