package com.example.member.infrastructure.client;

import com.example.member.infrastructure.client.dto.response.ApiResponse;
import com.example.member.infrastructure.client.dto.response.AuctionSellerBlockingSummaryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "auction-withdrawal-client",
        url = "${services.auction.url:http://localhost:8090}",
        path = "/internal/auctions"
)
public interface AuctionWithdrawalClient {

    @GetMapping("/sellers/{sellerId}/blocking-summary")
    ApiResponse<AuctionSellerBlockingSummaryResponse> getSellerBlockingSummary(@PathVariable UUID sellerId);
}
