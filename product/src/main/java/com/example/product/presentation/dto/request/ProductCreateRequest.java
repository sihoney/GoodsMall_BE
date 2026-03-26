package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 상품 등록 요청 DTO
 * sellerId는 API Gateway의 Header(X-User-Id)에서 전달받음
 */
public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        String title,
        String description,
        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 0보다 커야 합니다")
        BigDecimal price,
        @NotNull
        @PositiveOrZero
        Integer stockQuantity,
        @NotNull UUID categoryId
) {
}
