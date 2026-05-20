package com.example.order.infrastructure.kafka.consumer;

import com.example.order.application.service.OrderPaymentService;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.PaymentResultEvent;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderPaymentService orderPaymentService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_RESULT, groupId = "order-group", containerFactory = "paymentListenerContainerFactory")
    public void consume(EventEnvelope<PaymentResultEvent> envelope) {
        orderPaymentService.handlePaymentResult(envelope.payload());
    }
}
