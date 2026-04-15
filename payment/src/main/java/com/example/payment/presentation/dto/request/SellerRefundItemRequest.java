package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SellerRefundItemRequest(
        @NotNull(message = "orderItemId is required.")
        UUID orderItemId
) {
}
