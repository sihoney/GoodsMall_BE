package com.todaylunch.auction.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionCreateRequest(
        @NotNull
        UUID productId,

        @NotBlank
        String productTitle,

        @NotBlank
        String thumbnailKey,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal startPrice,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal bidUnit,

        @NotNull
        LocalDateTime startedAt,

        @NotNull
        @Min(1)
        Integer durationMinutes
) {
}
