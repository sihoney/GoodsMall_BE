package com.example.order.infrastructure.client;

import com.example.order.application.port.ProductPort;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.infrastructure.client.dto.ProductResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {

    private final ProductClient productClient;

    @Override
    public List<ProductInfo> getProductsByIds(List<UUID> productIds) {
        try {
            List<ProductResponse> responses = productClient.getProductsByIds(productIds);
            return responses.stream()
                    .map(res -> new ProductInfo(
                            res.productId(),
                            res.sellerId(),
                            res.name(),
                            res.price(),
                            res.productOrderStatus()
                    ))
                    .toList();
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
