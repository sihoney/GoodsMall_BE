package com.example.notification.infrastructure.messaging.kafka;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentResultEventConsumer {

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

        // 결제 결과에 따라 알림 생성
        if (event.status() == OrderPaymentResultStatus.SUCCESS) {
            notificationUsecase.createOrderPaymentSucceededNotification(
                    event.orderId(),
                    event.buyerMemberId(),
                    event.paidAmount(),
                    event.occurredAt()
            );
            return;
        }

        notificationUsecase.createOrderPaymentFailedNotification(
                event.orderId(),
                event.buyerMemberId(),
                event.failureReason(),
                event.occurredAt()
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
                && (event.paidAmount() == null || event.paidAmount() <= 0)) {
            throw new IllegalArgumentException("paidAmount must be positive for success.");
        }
        if (event.status() == OrderPaymentResultStatus.FAILED && event.failureReason() == null) {
            throw new IllegalArgumentException("failureReason is required for failure.");
        }
    }
}
