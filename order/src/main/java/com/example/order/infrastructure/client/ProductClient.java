package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

//TODO Discovery가 구현되면 url 제거
@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {

    @GetMapping("/api/products")
    List<ProductResponse> getProductsByIds(@RequestParam("productIds") List<UUID> productIds);
}
