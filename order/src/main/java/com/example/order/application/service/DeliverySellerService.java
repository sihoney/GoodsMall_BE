package com.example.order.application.service;

import com.example.order.application.usecase.DeliverySellerUseCase;
import com.example.order.domain.entity.CourierCompany;
import com.example.order.domain.entity.Delivery;
import com.example.order.domain.enumtype.DeliveryStatus;
import com.example.order.domain.repository.CourierRepository;
import com.example.order.domain.repository.DeliveryRepository;
import com.example.order.presentation.dto.response.DeliveryStatusCountResponse;
import com.example.order.presentation.dto.response.SellerDeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliverySellerService implements DeliverySellerUseCase {

    private final DeliveryRepository deliveryRepository;
    private final CourierRepository courierRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<SellerDeliveryResponse> getDeliveries(
            UUID sellerId,
            DeliveryStatus status,
            String orderNumber,
            String receiver,
            String productName,
            String courierName,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    ) {
        String courierCode = (courierName != null && !courierName.isBlank())
                ? courierRepository.findByNameAndActiveTrue(courierName)
                        .map(CourierCompany::getCode)
                        .orElse(null)
                : null;

        Page<Delivery> page = deliveryRepository.findBySellerIdWithFilters(
                sellerId,
                status,
                courierCode,
                orderNumber,
                receiver,
                productName,
                dateFrom != null ? dateFrom.atStartOfDay() : null,
                dateTo != null ? dateTo.atTime(LocalTime.MAX) : null,
                pageable
        );

        Set<String> codes = page.getContent().stream()
                .map(Delivery::getCourierCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> courierNames = codes.isEmpty()
                ? Map.of()
                : courierRepository.findNamesByCode(codes);

        return page.map(delivery ->
                SellerDeliveryResponse.from(delivery, courierNames.get(delivery.getCourierCode()))
        );
    }

    @Transactional(readOnly = true)
    @Override
    public DeliveryStatusCountResponse getStatusCounts(UUID sellerId) {
        Map<DeliveryStatus, Long> map = deliveryRepository.countByStatus(sellerId);
        return new DeliveryStatusCountResponse(
                map.getOrDefault(DeliveryStatus.PREPARING, 0L),
                map.getOrDefault(DeliveryStatus.SHIPPED, 0L),
                map.getOrDefault(DeliveryStatus.DELIVERED, 0L)
        );
    }
}
