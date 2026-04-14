package com.example.order.application.port;


import com.example.order.domain.enumtype.ProductOrderStatus;
import com.example.order.infrastructure.client.dto.request.ProductRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductPort {

    List<ProductInfo> deductStock(List<ProductRequest> productRequests);

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
