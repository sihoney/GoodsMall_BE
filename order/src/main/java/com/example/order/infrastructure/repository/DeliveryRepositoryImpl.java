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

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(2100, 12, 31, 23, 59, 59);

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
        LocalDateTime from = dateFrom != null ? dateFrom : MIN_DATE;
        LocalDateTime to   = dateTo   != null ? dateTo   : MAX_DATE;

        if (status != null) {
            return deliveryJpaRepository.findBySellerIdAndStatusWithFilters(
                    sellerId, status, courierCode,
                    like(orderNumber), like(receiver), like(productName),
                    from, to, pageable
            );
        }
        return deliveryJpaRepository.findBySellerIdWithFilters(
                sellerId, courierCode,
                like(orderNumber), like(receiver), like(productName),
                from, to, pageable
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
