package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.usecase.EscrowReleaseScheduleUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderDeliveryCompletedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 배송완료 이벤트를 escrow 자동 구매확정 예약 유스케이스로 연결하는 Kafka consumer다.
 */
public class OrderDeliveryCompletedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase;
    private final ObjectMapper objectMapper;

    public OrderDeliveryCompletedEventConsumer(EscrowReleaseScheduleUseCase escrowReleaseScheduleUseCase, ObjectMapper objectMapper) {
        this.escrowReleaseScheduleUseCase = escrowReleaseScheduleUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.order-delivery-completed:order.delivery-completed}",
            groupId = "${payment.kafka.consumer-groups.order-delivery-completed:payment-service}",
            containerFactory = "orderDeliveryCompletedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            OrderDeliveryCompletedMessage event = objectMapper.readValue(eventJson, OrderDeliveryCompletedMessage.class);
            validateEvent(event);
            escrowReleaseScheduleUseCase.scheduleRelease(new EscrowReleaseScheduleCommand(
                    event.orderId(),
                    toKoreaLocalDateTime(event.deliveredAt())
            ));
        } catch (Exception e) {
            log.error("Failed to process OrderDeliveryCompletedMessage", e);
            throw new RuntimeException("Failed to deserialize OrderDeliveryCompletedMessage", e);
        }
    }

    private void validateEvent(OrderDeliveryCompletedMessage event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("orderDeliveryCompleted event is required.");
        }
        if (event.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (event.deliveredAt() == null) {
            throw new InvalidOrderPaymentRequestException("deliveredAt is required.");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
