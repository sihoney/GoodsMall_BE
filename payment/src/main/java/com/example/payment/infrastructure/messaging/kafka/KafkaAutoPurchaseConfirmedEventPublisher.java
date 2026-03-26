package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaAutoPurchaseConfirmedEventPublisher implements AutoPurchaseConfirmedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaAutoPurchaseConfirmedEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.auto-purchase-confirmed:payment.auto-purchase-confirmed}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(AutoPurchaseConfirmedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.orderId()), new AutoPurchaseConfirmedMessage(
                event.orderId(),
                event.buyerMemberId(),
                event.confirmedAt()
        ));
    }
}
