package com.example.ai.application.service;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService implements RecommendationUseCase {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ProductEmbeddingRepository productEmbeddingRepository;

    @Override
    public List<RecommendedProductResult> recommend(UUID productId, int limit) {
        if (productId == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }

        int normalizedLimit = normalizeLimit(limit);

        ProductEmbedding source = productEmbeddingRepository.findByProductId(productId)
                .filter(ProductEmbedding::isActive)
                .orElseThrow(() -> new AiEmbeddingException("기준 상품의 활성 임베딩을 찾을 수 없습니다."));

        return productEmbeddingRepository.findSimilarActive(productId, source.getEmbedding(), normalizedLimit)
                .stream()
                .map(match -> new RecommendedProductResult(
                        match.productId(),
                        clampSimilarity(1.0d - match.distance())
                ))
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private double clampSimilarity(double score) {
        if (score < 0.0d) {
            return 0.0d;
        }
        if (score > 1.0d) {
            return 1.0d;
        }
        return score;
    }
}

