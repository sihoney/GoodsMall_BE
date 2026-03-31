package com.example.order.infrastructure.kafka;

import com.example.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(
                KafkaTopics.ORDER_CREATED,
                event.orderId().toString(),
                event
        );
    }
}
