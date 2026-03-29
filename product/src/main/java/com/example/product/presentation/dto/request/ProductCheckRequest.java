package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ProductCheckRequest(
    @NotNull(message = "상품 ID는 필수입니다")
    UUID productId,

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
    Integer quantity
) {
}
