package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionPriceRecommendationRequest(
        @Schema(description = "경매 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa010")
        @NotNull
        UUID auctionId,

        @Schema(description = "경매 상품 ID", example = "dddddddd-dddd-dddd-dddd-ddddddddd010")
        @NotNull
        UUID productId,

        @Schema(description = "현재 최고 입찰가", example = "72000")
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal currentBidPrice,

        @Schema(description = "경매 시작가", example = "50000")
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal startPrice,

        @Schema(description = "상품명", example = "한정판 콜라보 후드 (경매)")
        String productName,

        @Schema(description = "현재 입찰 횟수", example = "8")
        @PositiveOrZero
        Integer bidCount,

        @Schema(description = "남은 시간(초)", example = "3600")
        @PositiveOrZero
        Long remainingSeconds
) {

    public AuctionPriceRecommendationCommand toCommand() {
        return new AuctionPriceRecommendationCommand(
                auctionId,
                productId,
                currentBidPrice,
                startPrice,
                normalizeOptionalText(productName),
                bidCount,
                remainingSeconds
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        return trimmedValue;
    }
}

