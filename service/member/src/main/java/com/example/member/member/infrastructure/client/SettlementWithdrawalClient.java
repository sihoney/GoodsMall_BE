package com.example.member.member.infrastructure.client;

import com.example.member.member.infrastructure.client.dto.response.ApiResponse;
import com.example.member.member.infrastructure.client.dto.response.SettlementSellerWithdrawalSummaryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "settlement-withdrawal-client",
        url = "${services.settlement.url:http://localhost:8085}",
        path = "/internal/settlements"
)
public interface SettlementWithdrawalClient {

    @GetMapping("/sellers/{sellerId}/withdrawal-summary")
    ApiResponse<SettlementSellerWithdrawalSummaryResponse> getSellerWithdrawalSummary(@PathVariable UUID sellerId);
}
