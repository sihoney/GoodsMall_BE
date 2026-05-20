package com.example.ai.application.service;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import com.example.ai.domain.service.RecommendationReranker;
import com.example.ai.infrastructure.config.RecommendationCacheConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(
        cacheNames = RecommendationCacheConfig.RECOMMENDATION_CACHE_NAME,
        keyGenerator = "recommendationCacheKeyGenerator"
)
@Transactional(readOnly = true)
public class RecommendationService implements RecommendationUseCase {

    private final ProductEmbeddingRepository productEmbeddingRepository;
    private final RecommendationReranker recommendationReranker;

    @Override
    @Cacheable(sync = true)
    public List<RecommendedProductResult> recommend(UUID productId) {
        if (productId == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }

        ProductEmbedding source = productEmbeddingRepository.findByProductId(productId)
                .filter(ProductEmbedding::isActive)
                .orElseThrow(() -> new AiEmbeddingException("기준 상품의 활성 임베딩을 찾을 수 없습니다."));

        List<RecommendedProductResult> candidates = productEmbeddingRepository.findSimilarActive(
                        productId,
                        source.getEmbeddingLiteral(),
                        RecommendationLimitPolicy.CANDIDATE_LIMIT
                )
                .stream()
                .map(match -> new RecommendedProductResult(
                        match.productId(),
                        clampSimilarity(1.0d - match.distance())
                ))
                .toList();

        return selectRelatedTopResults(productId, candidates);
    }

    private List<RecommendedProductResult> selectRelatedTopResults(
            UUID baseProductId,
            List<RecommendedProductResult> candidates
    ) {
        int selectCount = Math.min(RecommendationLimitPolicy.RELATED_TOP_LIMIT, candidates.size());
        if (selectCount <= 0) {
            return List.of();
        }

        List<RecommendedProductResult> baseline = candidates.stream()
                .limit(selectCount)
                .toList();

        try {
            List<UUID> selectedIds = recommendationReranker.rerank(baseProductId, candidates, selectCount);
            return applySelectedOrder(candidates, selectedIds, selectCount, baseline);
        } catch (RuntimeException e) {
            log.warn("Recommendation rerank failed. fallback to similarity top5. baseProductId={}", baseProductId, e);
            return baseline;
        }
    }

    private List<RecommendedProductResult> applySelectedOrder(
            List<RecommendedProductResult> candidates,
            List<UUID> selectedIds,
            int selectCount,
            List<RecommendedProductResult> baseline
    ) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return baseline;
        }

        Map<UUID, RecommendedProductResult> candidateMap = new LinkedHashMap<>();
        for (RecommendedProductResult candidate : candidates) {
            candidateMap.put(candidate.productId(), candidate);
        }

        List<RecommendedProductResult> selected = new ArrayList<>();
        for (UUID selectedId : selectedIds) {
            RecommendedProductResult match = candidateMap.get(selectedId);
            if (match != null && doesNotContainProduct(selected, selectedId)) {
                selected.add(match);
            }
            if (selected.size() == selectCount) {
                return selected;
            }
        }

        for (RecommendedProductResult item : baseline) {
            if (doesNotContainProduct(selected, item.productId())) {
                selected.add(item);
            }
            if (selected.size() == selectCount) {
                break;
            }
        }

        return selected;
    }

    private boolean doesNotContainProduct(List<RecommendedProductResult> items, UUID productId) {
        for (RecommendedProductResult item : items) {
            if (item.productId().equals(productId)) {
                return false;
            }
        }
        return true;
    }

    private double clampSimilarity(double score) {
        if (score < 0.0d) {
            return 0.0d;
        }
        return Math.min(score, 1.0d);
    }
}
