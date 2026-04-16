package com.example.order.infrastructure.kafka.producer;

import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.OrderCanceledEvent;
import com.example.order.infrastructure.kafka.event.OrderCreatedEvent;
import com.example.order.infrastructure.kafka.event.OrderReturnRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreated(OrderCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    KafkaTopics.ORDER_CREATED,
                    event.orderId().toString(),
                    message
            );
        } catch (Exception e) {
            log.error("Failed to serialize OrderCreatedEvent. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize OrderCreatedEvent", e);
        }
    }

    public void sendOrderCanceled(OrderCanceledEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    KafkaTopics.ORDER_CANCELED,
                    event.orderId().toString(),
                    message
            );
        } catch (Exception e) {
            log.error("Failed to serialize OrderCanceledEvent. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize OrderCanceledEvent", e);
        }
    }

    public void sendOrderReturnRequested(OrderReturnRequestedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    KafkaTopics.ORDER_RETURN_REQUESTED,
                    event.orderId().toString(),
                    message
            );
        } catch (Exception e) {
            log.error("Failed to serialize OrderReturnRequestedEvent. orderId={}", event.orderId(), e);
            throw new RuntimeException("Failed to serialize OrderReturnRequestedEvent", e);
        }
    }
}
