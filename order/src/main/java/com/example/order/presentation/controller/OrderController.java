package com.example.order.presentation.controller;

import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.request.PaymentValidationRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import com.example.order.presentation.dto.response.PaymentValidationResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateUseCase orderCreateUseCase;
    private final OrderSearchUseCase orderSearchUseCase;

    @PostMapping("/deposit")
    public ResponseEntity<OrderCreateResponse> createByDeposit(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(orderCreateUseCase.createByDeposit(memberId, request));
    }

    @PostMapping("/pg")
    public ResponseEntity<OrderCreateResponse> createByPg(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(orderCreateUseCase.createByPg(memberId, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> findOrders(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @ParameterObject Pageable pageable
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(orderSearchUseCase.findByMemberId(memberId, pageable));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(orderSearchUseCase.getOrderDetail(orderId, memberId));
    }

    @PostMapping("/{orderId}/payment-validation")
    public ResponseEntity<PaymentValidationResponse> getPaymentValidation(
            @PathVariable UUID orderId,
            @Valid @RequestBody PaymentValidationRequest request
    ) {
        return ResponseEntity.ok(orderSearchUseCase.getPaymentValidation(orderId, request));
    }
}
