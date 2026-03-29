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

    private UUID productId;           // 상품 ID
    private UUID sellerId;            // 판매자 ID
    private String name;              // 상품 이름
    private BigDecimal price;         // 상품 가격
    private String thumbnailKeySnapshot;    // 사진 S3 key 주소
    private ProductOrderStatus productOrderStatus;  // 구매 가능 상태

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
        // 판매 불가 상태 (INACTIVE 또는 삭제된 상품)
        if (!product.isActive()) {
            return ProductOrderStatus.NOT_FOR_SALE;
        }

        // 재고 부족
        if (product.getStockQuantity() < requestedQuantity) {
            return ProductOrderStatus.INSUFFICIENT_STOCK;
        }

        // 구매 가능
        return ProductOrderStatus.ORDERABLE;
    }
}
