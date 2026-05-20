package com.example.member.member.infrastructure.client;

import com.example.member.member.infrastructure.client.dto.response.ApiResponse;
import com.example.member.member.infrastructure.client.dto.response.PaymentSellerWithdrawalSummaryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "payment-withdrawal-client",
        url = "${services.payment.url:http://localhost:8082}",
        path = "/internal/payments"
)
public interface PaymentWithdrawalClient {

    @GetMapping("/sellers/{sellerId}/withdrawal-summary")
    ApiResponse<PaymentSellerWithdrawalSummaryResponse> getSellerWithdrawalSummary(@PathVariable UUID sellerId);
}
