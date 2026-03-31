package com.example.product.presentation.dto.response;

import com.example.product.domain.entity.ProductImage;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductImageResponse(
        UUID imageId,
        String s3Key,
        String presignedUrl,
        Integer sortOrder,
        Boolean isThumbnail,
        LocalDateTime createdAt
) {
    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getImageId(),
                image.getS3Key(),
                null,  // presignedUrl은 서비스에서 생성
                image.getSortOrder(),
                image.isThumbnail(),
                image.getCreatedAt()
        );
    }

    public static ProductImageResponse from(ProductImage image, String presignedUrl) {
        return new ProductImageResponse(
                image.getImageId(),
                image.getS3Key(),
                presignedUrl,
                image.getSortOrder(),
                image.isThumbnail(),
                image.getCreatedAt()
        );
    }
}
