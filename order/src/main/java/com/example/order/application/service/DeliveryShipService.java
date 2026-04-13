package com.example.order.application.service;

import com.example.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryShipService {

    private static final String FAKE_COURIER_CODE = "04"; // CJ대한통운, 임의의 delivery 생성

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void startShip(UUID deliveryId) {
        deliveryRepository.findByDeliveryId(deliveryId).ifPresentOrElse(
                delivery -> {
                    delivery.ship(FAKE_COURIER_CODE, generateFakeInvoiceNumber());
                    log.info("배송 시작 - deliveryId: {}, invoiceNumber: {}, courierCode: {}",
                            deliveryId, delivery.getInvoiceNumber(), delivery.getCourierCode());
                },
                () -> log.warn("배송 정보를 찾을 수 없음 - deliveryId: {}", deliveryId)
        );

    }

    // 테스트용 배송 시작 송장 번호 생성
    private String generateFakeInvoiceNumber() {
        return String.valueOf(ThreadLocalRandom.current()
                .nextLong(100_000_000_000L, 1_000_000_000_000L));
    }
}