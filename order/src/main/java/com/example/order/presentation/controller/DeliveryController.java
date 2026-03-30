package com.example.order.presentation.controller;

import com.example.order.application.usecase.DeliveryTrackingUseCase;
import com.example.order.presentation.dto.response.DeliveryTrackingResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryTrackingUseCase deliveryTrackingUseCase;

    @GetMapping("/{deliveryId}/tracking")
    public ResponseEntity<DeliveryTrackingResponse> getTrackingInfo(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID deliveryId
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(deliveryTrackingUseCase.getTrackingInfo(deliveryId, memberId));
    }
}
