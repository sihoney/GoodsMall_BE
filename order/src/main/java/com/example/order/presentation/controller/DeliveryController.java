package com.example.order.presentation.controller;

import com.example.order.application.usecase.DeliveryShipUseCase;
import com.example.order.application.usecase.DeliveryTrackingUseCase;
import com.example.order.presentation.dto.request.DeliveryShipRequest;
import com.example.order.presentation.dto.response.ApiResponse;
import com.example.order.presentation.dto.response.DeliveryShipResponse;
import com.example.order.presentation.dto.response.DeliveryTrackingResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryTrackingUseCase deliveryTrackingUseCase;
    private final DeliveryShipUseCase deliveryShipUseCase;

    @GetMapping("/{deliveryId}/tracking")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> getTrackingInfo(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID deliveryId
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(deliveryTrackingUseCase.getTrackingInfo(deliveryId, memberId)));
    }

    @PostMapping("/{deliveryId}/ship")
    public ResponseEntity<ApiResponse<DeliveryShipResponse>> ship(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryShipRequest request
    ) {
        UUID sellerId = authenticatedMember.memberId();
        DeliveryShipResponse response = deliveryShipUseCase.startShip(deliveryId, sellerId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
