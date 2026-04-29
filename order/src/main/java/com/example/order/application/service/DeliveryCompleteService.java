package com.example.order.application.service;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Delivery;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryCompleteService {

    private final DeliveryRepository deliveryRepository;
    private final CacheManager cacheManager;

    @Transactional
    public void complete(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        delivery.complete();

        OrderItem orderItem = delivery.getOrderItem();
        orderItem.deliver();

        Order order = orderItem.getOrder();
        order.markDelivered();

        evictOrderDetailCache(order.getOrderId(), order.getBuyerId());

        log.info("배송 완료 처리. deliveryId={}, orderId={}", deliveryId, order.getOrderId());
    }

    private void evictOrderDetailCache(UUID orderId, UUID buyerId) {
        Cache cache = cacheManager.getCache("order:detail");
        if (cache != null) {
            cache.evict(orderId + ":" + buyerId);
        }
    }
}
