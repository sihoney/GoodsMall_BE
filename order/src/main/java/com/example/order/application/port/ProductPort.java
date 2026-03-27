package com.example.order.application.port;


import com.example.order.domain.enumtype.ProductOrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductPort {

    List<ProductInfo> getProductsByIds(List<UUID> productId);

    record ProductInfo(
            UUID productId,
            UUID sellerId,
            String name,
            BigDecimal price,
            String thumbnailKeySnapshot,
            ProductOrderStatus productOrderStatus
    ) {
    }
}
