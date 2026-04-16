package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.RecommendedProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/recommendations")
@Tag(name = "AI Recommendation", description = "연관 상품 추천 API")
public class RecommendationController {

    private final RecommendationUseCase recommendationUseCase;

    @GetMapping("/products/{productId}")
    @Operation(summary = "상품 연관 추천 조회")
    public ResponseEntity<ApiResponse<List<RecommendedProductResponse>>> recommendProducts(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<RecommendedProductResponse> response = recommendationUseCase.recommend(productId, limit)
                .stream()
                .map(RecommendedProductResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

