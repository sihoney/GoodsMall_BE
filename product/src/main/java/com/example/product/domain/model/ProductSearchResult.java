package com.example.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductSearchResult(
        UUID productId,
        UUID sellerId,
        UUID categoryId,
        String categoryName,
        String title,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String status,
        Integer viewCount,
        String thumbnailS3Key,
        LocalDateTime createdAt
) {
}
