package com.example.ai.presentation.controller;

import com.example.ai.application.usecase.AuctionPriceRecommendationUseCase;
import com.example.ai.presentation.dto.request.AuctionPriceRecommendationRequest;
import com.example.ai.presentation.dto.response.ApiResponse;
import com.example.ai.presentation.dto.response.AuctionPriceRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/ai")
@Tag(name = "AI Auction Price Recommendation", description = "경매 가격 추천 AI 내부 API (auction 서비스 전용)")
public class AuctionPriceRecommendationController {

    private final AuctionPriceRecommendationUseCase auctionPriceRecommendationUseCase;

    @PostMapping("/auction-price-recommendation")
    @Operation(
            summary = "경매 가격 추천",
            description = """
                    auction 서비스에서 내부적으로 호출하는 경매 가격 추천 API입니다.
                    경매 정보를 기반으로 예상 형성 가격과 추천 입찰가를 생성합니다.
                    필수 입력: auctionId, productId, currentBidPrice, startPrice
                    선택 입력: productName, bidCount, remainingSeconds (있으면 추천 품질 향상)
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "expectedFinalPrice": 128000,
                                        "recommendedBidPrice": 123000,
                                        "priceReason": "현재 입찰 흐름과 남은 시간을 기준으로 추가 상승 가능성이 있다고 판단했습니다.",
                                        "notes": "참고용 추천 가격이며 실제 낙찰가는 달라질 수 있습니다."
                                      },
                                      "error": null
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 입력 누락 또는 유효하지 않은 값)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID",
                                        "message": "currentBidPrice: 0보다 커야 합니다."
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "내부 처리 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "INTERNAL_SERVER_ERROR",
                                        "message": "서버 오류가 발생했습니다."
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuctionPriceRecommendationResponse>> recommendAuctionPrice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "경매 가격 추천 요청 예시",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "경매 가격 추천 요청", value = """
                                    {
                                      "auctionId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa010",
                                      "productId": "dddddddd-dddd-dddd-dddd-ddddddddd010",
                                      "currentBidPrice": 72000,
                                      "startPrice": 50000,
                                      "productName": "한정판 콜라보 후드 (경매)",
                                      "bidCount": 8,
                                      "remainingSeconds": 3600
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody AuctionPriceRecommendationRequest request
    ) {
        AuctionPriceRecommendationResponse response = AuctionPriceRecommendationResponse.from(
                auctionPriceRecommendationUseCase.recommend(request.toCommand())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

