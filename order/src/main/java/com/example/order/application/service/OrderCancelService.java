package com.example.order.application.service;

import com.example.order.application.port.dto.response.PaymentRefundResult;
import com.example.order.application.processor.PaymentProcessor;
import com.example.order.application.usecase.OrderCancelUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Claim;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.ClaimType;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.repository.ClaimRepository;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.kafka.event.OrderCanceledEvent;
import com.example.order.infrastructure.kafka.event.OrderReturnRequestedEvent;
import com.example.order.infrastructure.kafka.producer.OrderEventProducer;
import com.example.order.presentation.dto.request.OrderCancelRequest;
import com.example.order.presentation.dto.response.OrderCancelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderCancelService implements OrderCancelUseCase {

    private final OrderRepository orderRepository;
    private final ClaimRepository claimRepository;
    private final PaymentProcessor paymentProcessor;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderCancelResponse cancelOrder(UUID orderId, UUID memberId, OrderCancelRequest request) {
        Order order = findOrder(orderId);
        validateOrderOwner(order, memberId);

        Map<Boolean, List<OrderItem>> partitioned = partitionItems(order);
        List<OrderItem> canceledItems = partitioned.get(true);
        List<OrderItem> returnItems = partitioned.get(false);

        order.cancel(!returnItems.isEmpty());
        saveClaims(canceledItems, returnItems, request);

        if (!returnItems.isEmpty()) {
            publishReturnRequestedEvent(order, returnItems);
        }

        if (!canceledItems.isEmpty()) {
            PaymentRefundResult refundResult = paymentProcessor.refund(order, canceledItems, returnItems, request.reason());
            validateRefundResult(refundResult);
            publishCanceledEvent(order, canceledItems, refundResult);
            return new OrderCancelResponse(
                    order.getOrderId(),
                    refundResult.refundedAmount(),
                    LocalDateTime.ofInstant(refundResult.canceledAt(), ZoneId.systemDefault())
            );
        }

        return new OrderCancelResponse(order.getOrderId(), BigDecimal.ZERO, LocalDateTime.now());
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOrderOwner(Order order, UUID memberId) {
        if (!order.getBuyerId().equals(memberId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private void validateRefundResult(PaymentRefundResult refundResult) {
        if (refundResult.status() != PaymentStatus.SUCCESS) {
            throw new CustomException(ErrorCode.REFUND_FAILED);
        }
    }

    private Map<Boolean, List<OrderItem>> partitionItems(Order order) {
        return order.getItems().stream()
                .collect(Collectors.partitioningBy(OrderItem::cancel));
    }

    private void saveClaims(List<OrderItem> canceledItems, List<OrderItem> returnItems, OrderCancelRequest request) {
        List<Claim> claimsToSave = new ArrayList<>();
        claimsToSave.addAll(buildClaims(canceledItems, ClaimType.CANCEL, request));
        claimsToSave.addAll(buildClaims(returnItems, ClaimType.RETURN, request));
        claimRepository.saveAll(claimsToSave);
    }

    private List<Claim> buildClaims(List<OrderItem> items, ClaimType claimType, OrderCancelRequest request) {
        return items.stream()
                .map(item -> Claim.create(
                        item,
                        item.getSellerId(),
                        claimType,
                        request.reason(),
                        request.detailReason(),
                        request.requesterType()
                ))
                .toList();
    }

    private void publishCanceledEvent(Order order, List<OrderItem> canceledItems, PaymentRefundResult refundResult) {
        OrderCanceledEvent event = OrderCanceledEvent.of(order, canceledItems, refundResult.canceledAt());
        orderEventProducer.sendOrderCanceled(event);
    }

    private void publishReturnRequestedEvent(Order order, List<OrderItem> returnItems) {
        OrderReturnRequestedEvent event = OrderReturnRequestedEvent.of(order, returnItems);
        orderEventProducer.sendOrderReturnRequested(event);
    }
}
