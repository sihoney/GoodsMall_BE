package com.example.order.application.service;

import com.example.order.application.usecase.OrderConfirmUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.enumtype.OrderItemStatus;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConfirmService implements OrderConfirmUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "order:detail", key = "#orderId + ':' + #memberId")
    public void confirm(UUID orderId, UUID memberId) {
        Order order = orderRepository.findByOrderIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_CONFIRM);
        }

        order.complete();
        log.info("구매 확정 완료. orderId={}", orderId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "order:detail", key = "#orderId + ':' + #memberId")
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

        log.info("단품 구매 확정 완료. orderId={}, orderItemId={}", orderId, orderItemId);
    }
}
