package com.example.order.application.service;

import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.domain.entity.Order;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderSearchService implements OrderSearchUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderSummaryResponse> findByMemberId(UUID memberId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerId(memberId, pageable);
        return orders.map(OrderSummaryResponse::from);
    }
}
