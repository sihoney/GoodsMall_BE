package com.example.order.domain.repository;

import com.example.order.domain.entity.Delivery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {

    Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId);

    Optional<Delivery> findByDeliveryId(UUID deliveryId);

    Optional<Delivery> findByOrderItemId(UUID orderItemId);

    void saveAll(List<Delivery> deliveries);
}
