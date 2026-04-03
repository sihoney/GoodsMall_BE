package com.example.order.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemCreateRequest(

        @NotNull(message = "상품 ID는 필수입니다.")
        UUID productId,

        @NotNull(message = "수량은 1개 이상이어야 합니다.")
        @Min(1)
        Integer quantity
) {
}
