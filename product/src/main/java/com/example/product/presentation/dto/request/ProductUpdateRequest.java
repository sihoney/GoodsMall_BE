package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateRequest(
    @NotBlank(message = "상품명은 필수입니다")
    String title,

    String description,

    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 0보다 커야 합니다")
    BigDecimal price,

    @NotNull(message = "재고는 필수입니다")
    @PositiveOrZero(message = "재고는 0 이상이어야 합니다")
    Integer stockQuantity,

    @NotNull(message = "카테고리는 필수입니다")
    UUID categoryId
) {
}
