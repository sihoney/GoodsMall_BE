package com.example.notification.infrastructure.messaging.kafka.consumer;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentResultEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationUsecase notificationUsecase;

    public OrderPaymentResultEventConsumer(NotificationUsecase notificationUsecase) {
        this.notificationUsecase = notificationUsecase;
    }

    @KafkaListener(
            topics = "${notification.kafka.topics.order-payment-result:payment.order-payment-result}",
            groupId = "${notification.kafka.consumer-groups.order-payment-result:notification-service}",
            containerFactory = "orderPaymentResultKafkaListenerContainerFactory"
    )
    public void listen(OrderPaymentResultMessage event) {
        validateEvent(event);

        if (event.status() == OrderPaymentResultStatus.SUCCESS) {
            notificationUsecase.createOrderPaymentSucceededNotification(
                    event.orderId(),
                    event.buyerMemberId(),
                    toAmount(event.amount()),
                    toKoreaLocalDateTime(event.occurredAt())
            );
            return;
        }

        notificationUsecase.createOrderPaymentFailedNotification(
                event.orderId(),
                event.buyerMemberId(),
                event.reasonCode(),
                toKoreaLocalDateTime(event.occurredAt())
        );
    }

    private void validateEvent(OrderPaymentResultMessage event) {
        if (event == null) {
            throw new IllegalArgumentException("orderPaymentResult event is required.");
        }
        if (event.orderId() == null) {
            throw new IllegalArgumentException("orderId is required.");
        }
        if (event.buyerMemberId() == null) {
            throw new IllegalArgumentException("buyerMemberId is required.");
        }
        if (event.status() == null) {
            throw new IllegalArgumentException("status is required.");
        }
        if (event.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required.");
        }
        if (event.status() == OrderPaymentResultStatus.SUCCESS
                && (event.amount() == null || event.amount().signum() <= 0)) {
            throw new IllegalArgumentException("amount must be positive for success.");
        }
        if (event.status() == OrderPaymentResultStatus.FAILED && event.reasonCode() == null) {
            throw new IllegalArgumentException("reasonCode is required for failure.");
        }
    }

    private Long toAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("amount must be an exact whole amount.", e);
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
