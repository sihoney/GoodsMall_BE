package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.request.ExternalProductRequest;
import com.example.order.infrastructure.client.dto.response.ProductAvailabilityResponse;
import com.example.order.presentation.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${services.product.url:http://localhost:8081}", path = "/api/products")
public interface ProductClient {

    @PostMapping("/check-availability")
    ApiResponse<List<ProductAvailabilityResponse>> deductStock(@RequestBody List<ExternalProductRequest> productRequests);
}
