package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
/**
 * 주문 결제 결과 메시지를 Kafka 토픽으로 발행하는 adapter다.
 * 결과 메시지는 이미 consumer/application에서 완성된 계약 DTO를 그대로 전달한다.
 */
public class KafkaOrderPaymentResultEventPublisher implements OrderPaymentResultEventPublisher {

    private static final String ORDER_PAYMENT_RESULT_EVENT_TYPE = "ORDER_PAYMENT_RESULT";
    private static final String PAYMENT_SOURCE = "payment-service";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaOrderPaymentResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    /**
     * 주문 결제 결과 메시지를 orderId key로 발행한다.
     */
    public void publish(OrderPaymentResultMessage event) {
        try {
            EventEnvelope<OrderPaymentResultMessage> envelope = new EventEnvelope<>(
                    event.eventId(),
                    ORDER_PAYMENT_RESULT_EVENT_TYPE,
                    PAYMENT_SOURCE,
                    event.orderId(),
                    event.buyerMemberId(),
                    event.occurredAt(),
                    resolveTraceId(event),
                    event
            );
            String message = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.ORDER_PAYMENT_RESULT, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize OrderPaymentResultMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize OrderPaymentResultMessage", e);
        }
    }

    private String resolveTraceId(OrderPaymentResultMessage event) {
        UUID eventId = event.eventId();
        if (eventId == null) {
            return "payment-order-payment-result";
        }
        return eventId.toString();
    }
}
