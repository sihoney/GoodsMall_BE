package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리명은 필수입니다")
        String name,

        String description,

        @NotNull(message = "정렬순서는 필수입니다")
        Integer sortOrder,

        UUID parentId
) {
}
