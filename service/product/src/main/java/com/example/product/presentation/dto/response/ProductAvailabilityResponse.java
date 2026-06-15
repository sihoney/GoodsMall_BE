package com.example.product.presentation.dto.response;

import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductOrderStatus;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailabilityResponse {

    private UUID productId;
    private UUID sellerId;
    private String name;
    private BigDecimal price;
    private String thumbnailKeySnapshot;
    private ProductOrderStatus productOrderStatus;

    public static ProductAvailabilityResponse notForSale(UUID productId) {
        return new ProductAvailabilityResponse(
            productId, null, null, null, null,
            ProductOrderStatus.NOT_FOR_SALE
        );
    }

    public static ProductAvailabilityResponse of(
        Product product,
        Integer requestedQuantity,
        String thumbnailKeySnapshot
    ) {
        ProductOrderStatus status = determineOrderStatus(product, requestedQuantity);

        return new ProductAvailabilityResponse(
            product.getProductId(),
            product.getSellerId(),
            product.getTitle(),
            product.getPrice(),
            thumbnailKeySnapshot,
            status
        );
    }

    private static ProductOrderStatus determineOrderStatus(Product product, Integer requestedQuantity) {
        if (!product.isActive()) {
            return ProductOrderStatus.NOT_FOR_SALE;
        }

        if (product.getStockQuantity() < requestedQuantity) {
            return ProductOrderStatus.INSUFFICIENT_STOCK;
        }

        return ProductOrderStatus.ORDERABLE;
    }
}
