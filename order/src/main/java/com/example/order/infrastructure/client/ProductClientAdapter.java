package com.example.order.infrastructure.client;

import com.example.order.application.port.ProductPort;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.infrastructure.client.dto.response.ProductAvailabilityResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {

    private final ProductClient productClient;

    @Override
    public List<ProductInfo> checkAvailability(List<ProductRequest> productRequests) {
        try {
            List<ProductAvailabilityResponse> responses = productClient.checkAvailability(productRequests);
            return responses.stream()
                    .map(res -> new ProductInfo(
                            res.productId(),
                            res.sellerId(),
                            res.name(),
                            res.price(),
                            res.thumbnailKeySnapshot(),
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
