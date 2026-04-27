package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId);

    Optional<Delivery> findByOrderItemOrderItemId(UUID orderItemId);
}
