package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.service.OrderRefundResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaOrderRefundResultEventPublisher implements OrderRefundResultEventPublisher {

    private static final String ORDER_REFUND_RESULT_EVENT_TYPE = "ORDER_REFUND_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaOrderRefundResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderRefundResultMessage event) {
        try {
            EventEnvelope<OrderRefundResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    ORDER_REFUND_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.orderId(),
                    null,
                    event.occurredAt(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.ORDER_REFUND_RESULT, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("Failed to publish OrderRefundResultMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to publish OrderRefundResultMessage", e);
        }
    }

    private String resolveTraceId(OrderRefundResultMessage event) {
        UUID refundId = event.refundId();
        if (refundId != null) {
            return refundId.toString();
        }
        UUID eventId = event.eventId();
        if (eventId != null) {
            return eventId.toString();
        }
        return "payment-order-refund-result";
    }
}
