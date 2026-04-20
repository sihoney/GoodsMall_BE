package com.example.ai.presentation.dto.request;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionPriceRecommendationRequest(
        @NotNull
        UUID auctionId,

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal currentBidPrice,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal startPrice,

        String productName,

        @PositiveOrZero
        Integer bidCount,

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

