package com.example.notification.infrastructure.messaging.kafka.consumer;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 자동 구매확정 이벤트를 Kafka로부터 수신하여 알림을 생성하는 Consumer
@Component
public class AutoPurchaseConfirmedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationUsecase notificationUsecase;

    public AutoPurchaseConfirmedEventConsumer(NotificationUsecase notificationUsecase) {
        this.notificationUsecase = notificationUsecase;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.auto-purchase-confirmed:payment.auto-purchase-confirmed}",
            groupId = "${notification.kafka.consumer-groups.auto-purchase-confirmed:notification-service}",
            containerFactory = "autoPurchaseConfirmedKafkaListenerContainerFactory"
    )
    public void listen(AutoPurchaseConfirmedMessage event) {
        validateEvent(event);
        notificationUsecase.createAutoPurchaseConfirmedNotification(
                event.orderId(),
                event.buyerMemberId(),
                toKoreaLocalDateTime(event.confirmedAt())
        );
    }

    private void validateEvent(AutoPurchaseConfirmedMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("autoPurchaseConfirmed event is required.");
        }
        if (event.orderId() == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (event.buyerMemberId() == null) {
            throw new IllegalArgumentException("buyerMemberId is required.");
        }
        if (event.confirmedAt() == null) {
            throw new IllegalArgumentException("confirmedAt is required.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
