package com.example.product.presentation.dto.response;

import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 응답 DTO
 */
public record ProductResponse(
        UUID productId,
        String title,
        String description,
        BigDecimal price,
        Integer count,
        ProductStatus status,
        LocalDateTime createdAt
) {
    /**
     * Entity -> DTO 변환
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getCreatedAt()
        );
    }
}
