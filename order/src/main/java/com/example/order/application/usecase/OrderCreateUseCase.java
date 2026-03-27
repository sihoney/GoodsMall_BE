package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderCreateUseCase {
    OrderResponse create(UUID memberId, OrderCreateRequest request);
}
