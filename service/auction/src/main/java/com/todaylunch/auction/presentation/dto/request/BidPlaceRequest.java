package com.todaylunch.auction.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BidPlaceRequest(
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal bidPrice
) {
}
