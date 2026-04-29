package com.example.order.application.service;

import com.example.order.application.usecase.OrderSearchUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Delivery;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderType;
import java.time.LocalDateTime;
import com.example.order.domain.repository.DeliveryRepository;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.presentation.dto.request.PaymentValidationRequest;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderItemDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import com.example.order.presentation.dto.response.PaymentValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
    private final DeliveryRepository deliveryRepository;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    @Override
    public Page<OrderSummaryResponse> findByMemberId(UUID memberId, OrderType orderType, String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerIdAndOrderType(memberId, orderType, keyword, startDate, endDate, pageable);
        return orders.map(order -> OrderSummaryResponse.from(order, s3BaseUrl));
    }

    @Override
    @Cacheable(cacheNames = "order:detail", key = "#orderId + ':' + #memberId")
    public OrderDetailResponse getOrderDetail(UUID orderId, UUID memberId) {
        Order order = orderRepository.findByOrderIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItemDetailResponse> orderItems = order.getItems().stream()
                .map(item -> {
                    UUID deliveryId = deliveryRepository.findByOrderItemId(item.getOrderItemId())
                            .map(Delivery::getDeliveryId)
                            .orElse(null);
                    return OrderItemDetailResponse.from(item, deliveryId, s3BaseUrl);
                })
                .toList();

        return OrderDetailResponse.from(order, orderItems);
    }

    @Override
    public PaymentValidationResponse getPaymentValidation(UUID orderId, PaymentValidationRequest request) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (request.amount().compareTo(order.getTotalPrice()) != 0) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        return PaymentValidationResponse.from(order);
    }
}
