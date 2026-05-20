package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.UserRecommendationUseCase;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.RecommendedProductResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/recommendations")
@Tag(name = "AI Recommendation", description = "연관 상품 추천 API")
public class UserRecommendationController {

    private final UserRecommendationUseCase userRecommendationUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "사용자 맞춤 추천 조회",
            description = "장바구니에 담은 상품을 기반으로 사용자 맞춤 추천 상품을 조회합니다. 장바구니에 상품을 담은 이력이 없으면 빈 목록을 반환합니다."
    )
    public ResponseEntity<ApiResponse<List<RecommendedProductResponse>>> getMyRecommendations(
            @CurrentMember AuthenticatedMember member
    ) {
        List<RecommendedProductResponse> response = userRecommendationUseCase
                .getRecommendationsForUser(member.memberId())
                .stream()
                .map(RecommendedProductResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
