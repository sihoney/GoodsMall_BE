package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.OrderCancelRequest;
import com.example.order.presentation.dto.response.OrderCancelResponse;

import java.util.UUID;

public interface OrderCancelUseCase {

    OrderCancelResponse cancelOrder(UUID orderId, UUID memberId, OrderCancelRequest request);
}
