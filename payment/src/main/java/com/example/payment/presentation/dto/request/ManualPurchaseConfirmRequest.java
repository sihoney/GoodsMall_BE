package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ManualPurchaseConfirmRequest(
        @NotNull(message = "sellerMemberId is required.")
        UUID sellerMemberId
) {
}
