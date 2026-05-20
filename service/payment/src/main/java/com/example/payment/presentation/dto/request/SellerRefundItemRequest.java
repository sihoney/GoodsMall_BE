package com.example.payment.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SellerRefundItemRequest(
        @NotNull(message = "주문 항목 ID는 필수입니다.")
        UUID orderItemId
) {
}
