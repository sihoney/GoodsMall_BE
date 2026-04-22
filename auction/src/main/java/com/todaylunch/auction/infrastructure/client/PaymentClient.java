package com.todaylunch.auction.infrastructure.client;

import com.todaylunch.auction.infrastructure.client.dto.request.ClientBidFeeChargeRequest;
import com.todaylunch.auction.infrastructure.client.dto.response.ClientBidFeeChargeResponse;
import com.todaylunch.auction.presentation.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${services.payment.url}", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/auctions/bid-fees")
    ApiResponse<ClientBidFeeChargeResponse> chargeBidFee(@RequestBody ClientBidFeeChargeRequest request);
}
