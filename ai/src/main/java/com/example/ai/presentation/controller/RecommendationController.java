package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.RecommendationUseCase;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.RecommendedProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/recommendations")
@Tag(name = "AI Recommendation", description = "연관 상품 추천 API")
public class RecommendationController {

    private final RecommendationUseCase recommendationUseCase;

    @GetMapping("/products/{productId}")
    @Operation(
            summary = "상품 연관 추천 조회",
            description = "기준 상품 임베딩과 pgvector 유사도를 사용해 연관 상품 Top5를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "productId": "dddddddd-dddd-dddd-dddd-ddddddddd101",
                                          "similarityScore": 0.9998
                                        },
                                        {
                                          "productId": "dddddddd-dddd-dddd-dddd-ddddddddd102",
                                          "similarityScore": 0.9997
                                        },
                                        {
                                          "productId": "dddddddd-dddd-dddd-dddd-ddddddddd002",
                                          "similarityScore": 0.9996
                                        },
                                        {
                                          "productId": "dddddddd-dddd-dddd-dddd-ddddddddd103",
                                          "similarityScore": 0.9991
                                        },
                                        {
                                          "productId": "dddddddd-dddd-dddd-dddd-ddddddddd010",
                                          "similarityScore": 0.9988
                                        }
                                      ],
                                      "error": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "임베딩 처리 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    public ResponseEntity<ApiResponse<List<RecommendedProductResponse>>> recommendProducts(
            @Parameter(
                    description = "기준 상품 ID",
                    required = true,
                    example = "dddddddd-dddd-dddd-dddd-ddddddddd001"
            )
            @PathVariable UUID productId
    ) {
        List<RecommendedProductResponse> response = recommendationUseCase.recommend(productId)
                .stream()
                .map(RecommendedProductResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
