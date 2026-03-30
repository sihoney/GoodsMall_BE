package com.example.order.presentation.controller;

import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateUseCase orderCreateUseCase;
    private final OrderSearchUseCase orderSearchUseCase;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @RequestHeader(value = "X-User-Id") UUID memberId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderCreateUseCase.create(memberId, request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryResponse>> findOrders(
            @RequestHeader(value = "X-User-Id") UUID memberId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(orderSearchUseCase.findByMemberId(memberId, pageable));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @RequestHeader(value = "X-User-Id") UUID memberId,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderSearchUseCase.getOrderDetail(orderId, memberId));
    }
}
