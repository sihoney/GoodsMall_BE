package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryUpdateRequest(
        @NotBlank(message = "카테고리명은 필수입니다")
        String name,

        String description,

        @NotNull(message = "정렬 순서는 필수입니다")
        Integer sortOrder
) {
}
