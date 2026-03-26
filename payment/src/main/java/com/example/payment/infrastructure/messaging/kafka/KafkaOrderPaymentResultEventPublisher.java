package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderPaymentResultEventPublisher implements OrderPaymentResultEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaOrderPaymentResultEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.order-payment-result:payment.order-payment-result}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OrderPaymentResultMessage event) {
        kafkaTemplate.send(topic, String.valueOf(event.orderId()), event);
    }
}
