package com.example.order.presentation.controller;

import com.example.order.application.usecase.AuctionWinAcceptUseCase;
import com.example.order.application.usecase.OrderCancelUseCase;
import com.example.order.application.usecase.OrderConfirmUseCase;
import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.domain.enumtype.OrderType;
import com.example.order.presentation.dto.request.AuctionWinAcceptRequest;
import com.example.order.presentation.dto.request.OrderCancelRequest;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.request.PaymentValidationRequest;
import com.example.order.presentation.dto.response.ApiResponse;
import com.example.order.presentation.dto.response.OrderCancelResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateUseCase orderCreateUseCase;
    private final OrderSearchUseCase orderSearchUseCase;
    private final OrderCancelUseCase orderCancelUseCase;
    private final OrderConfirmUseCase orderConfirmUseCase;
    private final AuctionWinAcceptUseCase auctionWinAcceptUseCase;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createByDeposit(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(orderCreateUseCase.createByDeposit(memberId, request)));
    }

    @PostMapping("/pg")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createByPg(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(orderCreateUseCase.createByPg(memberId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> findOrders(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject Pageable pageable
    ) {
        UUID memberId = authenticatedMember.memberId();
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        return ResponseEntity.ok(ApiResponse.success(orderSearchUseCase.findByMemberId(memberId, orderType, keyword, startDateTime, endDateTime, pageable)));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(orderSearchUseCase.getOrderDetail(orderId, memberId)));
    }

    @PostMapping("/{orderId}/payment-validation")
    public ResponseEntity<ApiResponse<PaymentValidationResponse>> getPaymentValidation(
            @PathVariable UUID orderId,
            @Valid @RequestBody PaymentValidationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderSearchUseCase.getPaymentValidation(orderId, request)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderCancelRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        return ResponseEntity.ok(ApiResponse.success(orderCancelUseCase.cancelOrder(orderId, memberId, request)));
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId
    ) {
        UUID memberId = authenticatedMember.memberId();
        orderConfirmUseCase.confirm(orderId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{orderId}/items/{orderItemId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOrderItem(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId
    ) {
        UUID memberId = authenticatedMember.memberId();
        orderConfirmUseCase.confirmItem(orderId, orderItemId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/auction/deposit")
    public ResponseEntity<Void> acceptAuctionWinByDeposit(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody AuctionWinAcceptRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        auctionWinAcceptUseCase.acceptWinByDeposit(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auction/pg")
    public ResponseEntity<Void> acceptAuctionWinByPg(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody AuctionWinAcceptRequest request
    ) {
        UUID memberId = authenticatedMember.memberId();
        auctionWinAcceptUseCase.acceptWinByPg(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
