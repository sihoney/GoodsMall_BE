package com.example.order.domain.repository;

import com.example.order.domain.entity.Delivery;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {

    Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId);
}
