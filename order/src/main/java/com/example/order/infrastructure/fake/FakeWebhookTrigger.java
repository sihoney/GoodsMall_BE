package com.example.order.infrastructure.fake;

import com.example.order.application.service.DeliveryCompleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakeWebhookTrigger {

    private static final int DELIVERED_AFTER_SECONDS = 10;

    private final TaskScheduler taskScheduler;
    private final DeliveryCompleteService deliveryCompleteService;

    public void scheduleDeliveryComplete(UUID deliveryId) {
        Instant triggerAt = Instant.now().plusSeconds(DELIVERED_AFTER_SECONDS);
        taskScheduler.schedule(
                () -> {
                    try {
                        deliveryCompleteService.complete(deliveryId);
                        log.info("가상 웹훅 발동 - 배송 완료 처리. deliveryId={}", deliveryId);
                    } catch (Exception e) {
                        log.error("가상 웹훅 처리 실패. deliveryId={}", deliveryId, e);
                    }
                },
                triggerAt
        );
        log.info("가상 웹훅 예약 완료. deliveryId={}, {}초 후 발동", deliveryId, DELIVERED_AFTER_SECONDS);
    }
}
