package com.example.order.application.usecase;

import com.example.order.domain.enumtype.OrderType;
import com.example.order.presentation.dto.request.PaymentValidationRequest;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import com.example.order.presentation.dto.response.PaymentValidationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderSearchUseCase {
    Page<OrderSummaryResponse> findByMemberId(UUID memberId, OrderType orderType, Pageable pageable);

    OrderDetailResponse getOrderDetail(UUID orderId, UUID memberId);

    PaymentValidationResponse getPaymentValidation(UUID orderId, PaymentValidationRequest request);
}
