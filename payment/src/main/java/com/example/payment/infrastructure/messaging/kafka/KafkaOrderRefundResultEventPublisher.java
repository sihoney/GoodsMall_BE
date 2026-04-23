package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.service.OrderRefundResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderRefundResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaOrderRefundResultEventPublisher implements OrderRefundResultEventPublisher {

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
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.ORDER_REFUND_RESULT, String.valueOf(event.orderId()), message);
        } catch (Exception e) {
            log.error("Failed to publish OrderRefundResultMessage. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to publish OrderRefundResultMessage", e);
        }
    }
}
