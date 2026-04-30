package com.example.ai.application.service;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.application.usecase.UserRecommendationUseCase;
import com.example.ai.infrastructure.recommendation.UserRecommendationRedisRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecommendationService implements UserRecommendationUseCase {

    private final RecommendationUseCase recommendationUseCase;
    private final UserRecommendationRedisRepository userRecommendationRedisRepository;

    @Override
    public void updateRecommendationsForUser(UUID memberId, UUID productId) {
        List<RecommendedProductResult> results = recommendationUseCase.recommend(productId);
        userRecommendationRedisRepository.save(memberId, results);
        log.debug("사용자 추천 업데이트 완료 memberId={} productId={} count={}", memberId, productId, results.size());
    }

    @Override
    public List<RecommendedProductResult> getRecommendationsForUser(UUID memberId) {
        return userRecommendationRedisRepository.findByMemberId(memberId);
    }
}
