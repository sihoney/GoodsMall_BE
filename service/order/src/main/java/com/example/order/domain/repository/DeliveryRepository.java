package com.example.order.domain.repository;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.enumtype.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {

    Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId);

    Optional<Delivery> findByDeliveryId(UUID deliveryId);

    Optional<Delivery> findByOrderItemId(UUID orderItemId);

    void saveAll(List<Delivery> deliveries);

    Page<Delivery> findBySellerIdWithFilters(
            UUID sellerId,
            DeliveryStatus status,
            String courierCode,
            String orderNumber,
            String receiver,
            String productName,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );

    Map<DeliveryStatus, Long> countByStatus(UUID sellerId);
}
