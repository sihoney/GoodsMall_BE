package com.example.order.application.service;

import com.example.order.application.usecase.OrderConfirmUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.enumtype.OrderItemStatus;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.repository.OutboxRepository;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConfirmService implements OrderConfirmUseCase {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void confirm(UUID orderId, UUID memberId) {
        Order order = orderRepository.findByOrderIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_CONFIRM);
        }

        order.complete();
        saveOrderCompletedOutboxPerSeller(order);
        log.info("구매 확정 완료. orderId={}", orderId);
    }

    @Override
    @Transactional
    public void confirmItem(UUID orderId, UUID orderItemId, UUID memberId) {
        Order order = orderRepository.findByOrderIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (orderItem.getStatus() != OrderItemStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_ITEM_CANNOT_CONFIRM);
        }

        orderItem.complete();
        order.completeIfAllItemsCompleted();

        if (order.getStatus() == OrderStatus.COMPLETED) {
            saveOrderCompletedOutboxPerSeller(order);
        }

        log.info("단품 구매 확정 완료. orderId={}, orderItemId={}", orderId, orderItemId);
    }

    private void saveOrderCompletedOutboxPerSeller(Order order) {
        List<UUID> sellerIds = order.getItems().stream()
                .map(OrderItem::getSellerId)
                .distinct()
                .toList();

        for (UUID sellerId : sellerIds) {
            try {
                String payload = objectMapper.writeValueAsString(OrderCompletedEvent.envelopeOf(order, sellerId));
                outboxRepository.save(OutboxEvent.create(KafkaTopics.ORDER_PURCHASE_CONFIRMED, order.getOrderId().toString(), payload));
            } catch (Exception e) {
                log.error("OrderCompletedEvent Outbox 저장 실패. orderId={}, sellerId={}", order.getOrderId(), sellerId, e);
            }
        }
    }
}
