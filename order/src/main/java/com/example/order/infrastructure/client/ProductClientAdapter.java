package com.example.order.infrastructure.client;

import com.example.order.application.port.ProductPort;
import com.example.order.application.port.dto.request.ProductStockDeductRequest;
import com.example.order.application.port.dto.response.ProductInfo;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.infrastructure.client.dto.request.ExternalProductRequest;
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
    public List<ProductInfo> deductStock(List<ProductStockDeductRequest> requests) {
        try {
            List<ExternalProductRequest> externalRequests = requests.stream()
                    .map(ExternalProductRequest::from)
                    .toList();
            List<ProductAvailabilityResponse> responses = productClient.deductStock(externalRequests);
            return responses.stream()
                    .map(this::toProductInfo)
                    .toList();
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ProductInfo toProductInfo(ProductAvailabilityResponse res) {
        return new ProductInfo(
                res.productId(),
                res.sellerId(),
                res.name(),
                res.price(),
                res.thumbnailKeySnapshot(),
                res.productOrderStatus()
        );
    }
}
