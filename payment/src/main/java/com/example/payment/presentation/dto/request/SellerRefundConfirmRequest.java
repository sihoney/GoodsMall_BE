package com.example.payment.presentation.dto.request;

import com.example.payment.domain.enumtype.PaymentRefundType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SellerRefundConfirmRequest(
        @NotNull(message = "orderId is required.")
        UUID orderId,

        @NotNull(message = "orderCancelRequestId is required.")
        UUID orderCancelRequestId,

        @NotNull(message = "refundType is required.")
        PaymentRefundType refundType,

        String reason,

        @NotEmpty(message = "items must not be empty.")
        List<@Valid SellerRefundItemRequest> items
) {
}
