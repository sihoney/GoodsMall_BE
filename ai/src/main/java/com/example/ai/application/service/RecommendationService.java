package com.example.ai.application.service;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import com.example.ai.infrastructure.config.RecommendationCacheConfig;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(
        cacheNames = RecommendationCacheConfig.RECOMMENDATION_CACHE_NAME,
        keyGenerator = "recommendationCacheKeyGenerator"
)
@Transactional(readOnly = true)
public class RecommendationService implements RecommendationUseCase {

    private final ProductEmbeddingRepository productEmbeddingRepository;

    @Override
    @Cacheable(sync = true)
    public List<RecommendedProductResult> recommend(UUID productId) {
        if (productId == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }

        ProductEmbedding source = productEmbeddingRepository.findByProductId(productId)
                .filter(ProductEmbedding::isActive)
                .orElseThrow(() -> new AiEmbeddingException("기준 상품의 활성 임베딩을 찾을 수 없습니다."));

        return productEmbeddingRepository.findSimilarActive(
                        productId,
                        source.getEmbedding(),
                        RecommendationLimitPolicy.CANDIDATE_LIMIT
                )
                .stream()
                .map(match -> new RecommendedProductResult(
                        match.productId(),
                        clampSimilarity(1.0d - match.distance())
                ))
                .limit(RecommendationLimitPolicy.RELATED_TOP_LIMIT)
                .toList();
    }

    private double clampSimilarity(double score) {
        if (score < 0.0d) {
            return 0.0d;
        }
        return Math.min(score, 1.0d);
    }
}
