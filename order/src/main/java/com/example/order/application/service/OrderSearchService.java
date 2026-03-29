package com.example.order.application.service;

import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderItemDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    public OrderDetailResponse getOrderDetail(UUID orderId, UUID memberId) {
        Order order = orderRepository.findByOrderIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItemDetailResponse> orderItems = order.getItems().stream()
                .map(OrderItemDetailResponse::from)
                .toList();

        return OrderDetailResponse.from(order, orderItems);
    }
}
