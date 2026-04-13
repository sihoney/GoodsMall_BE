package com.example.order.application.processor;

import com.example.order.application.port.ProductPort;
import com.example.order.application.port.ProductPort.ProductInfo;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.enumtype.ProductOrderStatus;
import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductProcessor {

    private final ProductPort productPort;

    public void validateDuplicate(List<ProductRequest> productRequests) {
        Set<UUID> requestedProductIds = productRequests.stream()
                .map(ProductRequest::productId)
                .collect(Collectors.toSet());

        if (requestedProductIds.size() != productRequests.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_PRODUCT_REQUEST);
        }
    }

    public Map<UUID, ProductInfo> deductStock(List<ProductRequest> productRequests) {
        List<ProductInfo> products = productPort.deductStock(productRequests);

        if (products.size() != productRequests.size()) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return products.stream()
                .collect(Collectors.toMap(ProductInfo::productId, p -> p));
    }

    public void validateStatus(Map<UUID, ProductInfo> productMap, OrderCreateRequest request) {
        request.orderItemRequest().forEach(item -> {
            ProductInfo product = productMap.get(item.productId());
            ProductOrderStatus status = product.productOrderStatus();

            if (status == ProductOrderStatus.INSUFFICIENT_STOCK) {
                throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
            } else if (status == ProductOrderStatus.NOT_FOR_SALE) {
                throw new CustomException(ErrorCode.PRODUCT_NOT_ORDERABLE);
            }
        });
    }

}
