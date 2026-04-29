package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.enumtype.DeliveryStatus;
import com.example.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Page<Delivery> findBySellerIdWithFilters(
            UUID sellerId,
            DeliveryStatus status,
            String courierCode,
            String orderNumber,
            String receiver,
            String productName,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        if (status != null) {
            return deliveryJpaRepository.findBySellerIdAndStatusWithFilters(
                    sellerId, status, courierCode,
                    like(orderNumber), like(receiver), like(productName),
                    dateFrom, dateTo, pageable
            );
        }
        return deliveryJpaRepository.findBySellerIdWithFilters(
                sellerId, courierCode,
                like(orderNumber), like(receiver), like(productName),
                dateFrom, dateTo, pageable
        );
    }

    private String like(String value) {
        return (value != null && !value.isBlank()) ? "%" + value.trim() + "%" : null;
    }

    @Override
    public Map<DeliveryStatus, Long> countByStatus(UUID sellerId) {
        List<Object[]> rows = deliveryJpaRepository.countBySellerIdGroupByStatus(sellerId);
        Map<DeliveryStatus, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((DeliveryStatus) row[0], (Long) row[1]);
        }
        return map;
    }
}
