package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;

    @Override
    public Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId) {
        return deliveryJpaRepository.findByDeliveryIdAndBuyerId(deliveryId, buyerId);
    }

    @Override
    public Optional<Delivery> findByDeliveryId(UUID deliveryId) {
        return deliveryJpaRepository.findById(deliveryId);
    }

    @Override
    public Optional<Delivery> findByOrderItemId(UUID orderItemId) {
        return deliveryJpaRepository.findByOrderItemOrderItemId(orderItemId);
    }

    @Override
    public void saveAll(List<Delivery> deliveries) {
        deliveryJpaRepository.saveAll(deliveries);
    }
}
