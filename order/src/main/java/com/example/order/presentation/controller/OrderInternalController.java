package com.example.order.presentation.controller;

import com.example.order.application.usecase.DeliverySellerUseCase;
import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.presentation.dto.response.ApiResponse;
import com.example.order.presentation.dto.response.DeliveryStatusCountResponse;
import com.example.order.presentation.dto.response.MemberOrderWithdrawalSummaryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderSearchUseCase orderSearchUseCase;
    private final DeliverySellerUseCase deliverySellerUseCase;

    @GetMapping("/orders/members/{memberId}/withdrawal-summary")
    public ResponseEntity<ApiResponse<MemberOrderWithdrawalSummaryResponse>> getMemberWithdrawalSummary(
            @PathVariable UUID memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderSearchUseCase.getWithdrawalSummary(memberId)));
    }

    @GetMapping("/deliveries/sellers/{sellerId}/status-counts")
    public ResponseEntity<ApiResponse<DeliveryStatusCountResponse>> getSellerDeliveryStatusCounts(
            @PathVariable UUID sellerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(deliverySellerUseCase.getStatusCounts(sellerId)));
    }
}
