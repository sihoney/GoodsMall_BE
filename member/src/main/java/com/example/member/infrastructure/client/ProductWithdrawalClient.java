package com.example.member.infrastructure.client;

import com.example.member.infrastructure.client.dto.response.ProductSellerWithdrawalSummaryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-withdrawal-client",
        url = "${services.product.url:http://localhost:8081}",
        path = "/internal/products"
)
public interface ProductWithdrawalClient {

    @GetMapping("/sellers/{sellerId}/withdrawal-summary")
    ProductSellerWithdrawalSummaryResponse getSellerWithdrawalSummary(@PathVariable UUID sellerId);
}
