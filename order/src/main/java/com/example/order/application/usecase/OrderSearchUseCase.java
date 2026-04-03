package com.example.order.application.usecase;

import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderSearchUseCase {
    Page<OrderSummaryResponse> findByMemberId(UUID memberId, Pageable pageable);

    OrderDetailResponse getOrderDetail(UUID orderId, UUID memberId);
}
