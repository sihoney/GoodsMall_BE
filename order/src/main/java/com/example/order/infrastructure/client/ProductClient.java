package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.infrastructure.client.dto.response.ProductAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//TODO Discovery가 구현되면 url 제거
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {

    @PostMapping("/api/products/check-availability")
    List<ProductAvailabilityResponse> checkAvailability(@RequestBody List<ProductRequest> productRequests);
}
