package com.example.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 상품 이미지 등록 요청 DTO
 */
public record ProductImageRequest(
        @NotBlank(message = "S3 키는 필수입니다")
        String s3Key,

        @NotNull(message = "정렬 순서는 필수입니다")
        @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다")
        Integer sortOrder,

        @NotNull(message = "썸네일 여부는 필수입니다")
        Boolean isThumbnail
) {
}
