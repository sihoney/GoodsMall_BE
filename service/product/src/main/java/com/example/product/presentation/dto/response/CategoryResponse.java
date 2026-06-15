package com.example.product.presentation.dto.response;

import com.example.product.domain.entity.Category;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 카테고리 응답 DTO (Flat List 방식)
 */
public record CategoryResponse(
        UUID categoryId,
        UUID sellerId,
        String name,
        String description,
        Integer depth,
        Integer sortOrder,
        UUID parentId,
        LocalDateTime createdAt
) {
    /**
     * Entity -> DTO 변환
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getSellerId(),
                category.getName(),
                category.getDescription(),
                category.getDepth(),
                category.getSortOrder(),
                category.getParent() != null ? category.getParent().getCategoryId() : null,
                category.getCreatedAt()
        );
    }
}
