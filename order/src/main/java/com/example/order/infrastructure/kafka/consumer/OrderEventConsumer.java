package com.example.order.infrastructure.kafka.consumer;

import com.example.order.application.service.OrderPaymentService;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderPaymentService orderPaymentService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_RESULT,
            groupId = "order-group")
    public void consume(PaymentResultEvent event) {
        orderPaymentService.handlePaymentResult(event);
    }
}
