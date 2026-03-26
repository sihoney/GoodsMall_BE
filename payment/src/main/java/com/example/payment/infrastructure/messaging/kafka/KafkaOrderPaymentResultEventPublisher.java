package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.domain.service.OrderPaymentResultEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
/**
 * 주문 결제 결과 메시지를 Kafka 토픽으로 발행하는 adapter다.
 * 결과 메시지는 이미 consumer/application에서 완성된 계약 DTO를 그대로 전달한다.
 */
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
    /**
     * 주문 결제 결과 메시지를 orderId key로 발행한다.
     */
    public void publish(OrderPaymentResultMessage event) {
        kafkaTemplate.send(topic, String.valueOf(event.orderId()), event);
    }
}
