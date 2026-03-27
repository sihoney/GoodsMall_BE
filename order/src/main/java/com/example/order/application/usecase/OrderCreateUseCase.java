package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;

import java.util.UUID;

public interface OrderCreateUseCase {
    OrderCreateResponse create(UUID memberId, OrderCreateRequest request);
}
