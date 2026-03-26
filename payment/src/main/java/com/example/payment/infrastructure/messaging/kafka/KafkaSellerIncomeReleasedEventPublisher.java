package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SellerIncomeReleasedEvent;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerIncomeReleasedMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSellerIncomeReleasedEventPublisher implements SellerIncomeReleasedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaSellerIncomeReleasedEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.seller-income-released:payment.seller-income-released}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(SellerIncomeReleasedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.orderId()), new SellerIncomeReleasedMessage(
                event.orderId(),
                event.sellerMemberId(),
                event.sellerWalletId(),
                event.releasedAmount(),
                event.releasedAt(),
                event.confirmationType()
        ));
    }
}
