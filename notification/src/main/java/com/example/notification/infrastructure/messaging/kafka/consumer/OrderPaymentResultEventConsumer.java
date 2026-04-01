package com.example.notification.infrastructure.messaging.kafka.consumer;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.notification.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
/**
 * payment의 주문 결제 결과 이벤트를 notification 유스케이스로 연결하는 consumer다.
 * 주문 단위 result 계약만 해석하고, 내부 알림 모델에 맞춰 시간과 금액만 변환한다.
 */
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
    /**
     * 주문 결제 결과를 성공/실패 알림 생성 요청으로 위임한다.
     */
    public void listen(OrderPaymentResultMessage event) {
        validateEvent(event);

        if (event.status() == OrderPaymentResultStatus.SUCCESS) {
            notificationUsecase.createOrderPaymentSucceededNotification(
                    event.orderId(),
                    event.buyerMemberId(),
                    toAmount(event.amount()),
                    toUtcLocalDateTime(event.occurredAt())
            );
            return;
        }

        notificationUsecase.createOrderPaymentFailedNotification(
                event.orderId(),
                event.buyerMemberId(),
                event.reasonCode(),
                toUtcLocalDateTime(event.occurredAt())
        );
    }

    /**
     * 주문 결제 결과 계약의 필수 필드를 검증한다.
     */
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

    /**
     * 알림 애플리케이션은 정수 금액 모델을 사용하므로 계약 BigDecimal 금액을 변환한다.
     */
    private Long toAmount(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("amount must be an exact whole amount.", e);
        }
    }

    /**
     * notification 내부 모델을 유지하기 위해 Kafka UTC 시간을 LocalDateTime으로 변환한다.
     */
    private LocalDateTime toUtcLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
