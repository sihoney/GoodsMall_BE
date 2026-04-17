package com.example.product.presentation.dto.response;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.enumtype.ProductType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
        ProductType type,
        UUID categoryId,
        String categoryName,
        LocalDateTime createdAt,
        List<ProductImageResponse> images
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getType(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                Collections.emptyList()
        );
    }

    public static ProductResponse from(Product product, List<ProductImage> images) {
        List<ProductImageResponse> imageResponses = images.stream()
                .map(ProductImageResponse::from)
                .toList();

        return new ProductResponse(
                product.getProductId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getType(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                imageResponses
        );
    }
}
