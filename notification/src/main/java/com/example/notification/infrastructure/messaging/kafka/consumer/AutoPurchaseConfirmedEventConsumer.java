package com.example.notification.infrastructure.messaging.kafka;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// 자동 구매확정 이벤트를 Kafka로부터 수신하여 알림을 생성하는 Consumer
@Component
public class AutoPurchaseConfirmedEventConsumer {

    private final NotificationUsecase notificationUsecase;

    public AutoPurchaseConfirmedEventConsumer(NotificationUsecase notificationUsecase) {
        this.notificationUsecase = notificationUsecase;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.auto-purchase-confirmed:payment.auto-purchase-confirmed}", // application.yml에서 설정한 토픽 이름 사용
            groupId = "${notification.kafka.consumer-groups.auto-purchase-confirmed:notification-service}", // application.yml에서 설정한 Consumer Group 이름 사용
            containerFactory = "autoPurchaseConfirmedKafkaListenerContainerFactory" // KafkaListenerContainerFactory 지정
    )
    public void listen(AutoPurchaseConfirmedMessage event) {
        validateEvent(event);
        notificationUsecase.createAutoPurchaseConfirmedNotification(
                event.orderId(),
                event.buyerMemberId(),
                toUtcLocalDateTime(event.confirmedAt())
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

    /**
     * Kafka 계약의 UTC Instant를 notification 내부에서 사용하는 LocalDateTime으로 변환한다.
     */
    private LocalDateTime toUtcLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
