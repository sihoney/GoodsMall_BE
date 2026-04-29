package com.example.order.presentation.controller;

import com.example.order.application.usecase.DeliverySellerUseCase;
import com.example.order.application.usecase.DeliveryShipUseCase;
import com.example.order.application.usecase.DeliveryTrackingUseCase;
import com.example.order.presentation.dto.request.DeliveryShipRequest;
import com.example.order.presentation.dto.response.ApiResponse;
import com.example.order.presentation.dto.response.DeliveryShipResponse;
import com.example.order.presentation.dto.response.DeliveryStatusCountResponse;
import com.example.order.presentation.dto.response.DeliveryTrackingResponse;
import com.example.order.presentation.dto.response.SellerDeliveryResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.example.order.domain.enumtype.DeliveryStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryTrackingUseCase deliveryTrackingUseCase;
    private final DeliveryShipUseCase deliveryShipUseCase;
    private final DeliverySellerUseCase deliverySellerUseCase;

    @GetMapping("/{deliveryId}/tracking")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> getTrackingInfo(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID deliveryId
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(deliveryTrackingUseCase.getTrackingInfo(deliveryId, memberId)));
    }

    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<Page<SellerDeliveryResponse>>> getDeliveries(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String courierName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UUID sellerId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(
                deliverySellerUseCase.getDeliveries(
                        sellerId, status, orderNumber, receiver, productName, courierName, dateFrom, dateTo, pageable
                )
        ));
    }

    @GetMapping("/seller/counts")
    public ResponseEntity<ApiResponse<DeliveryStatusCountResponse>> getStatusCounts(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        UUID sellerId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(deliverySellerUseCase.getStatusCounts(sellerId)));
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