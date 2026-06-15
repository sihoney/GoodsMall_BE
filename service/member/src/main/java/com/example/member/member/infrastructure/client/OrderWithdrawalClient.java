package com.example.member.member.infrastructure.client;

import com.example.member.member.infrastructure.client.dto.response.ApiResponse;
import com.example.member.member.infrastructure.client.dto.response.DeliveryStatusCountResponse;
import com.example.member.member.infrastructure.client.dto.response.MemberOrderWithdrawalSummaryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-withdrawal-client", url = "${services.order.url:http://localhost:8084}")
public interface OrderWithdrawalClient {

    @GetMapping("/internal/orders/members/{memberId}/withdrawal-summary")
    ApiResponse<MemberOrderWithdrawalSummaryResponse> getMemberWithdrawalSummary(@PathVariable UUID memberId);

    @GetMapping("/internal/deliveries/sellers/{sellerId}/status-counts")
    ApiResponse<DeliveryStatusCountResponse> getSellerDeliveryStatusCounts(@PathVariable UUID sellerId);
}
