package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.domain.service.SettlementCandidateCreatedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaSettlementCandidateCreatedEventPublisher implements SettlementCandidateCreatedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaSettlementCandidateCreatedEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.settlement-candidate-created:payment.settlement-candidate-created}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(SettlementCandidateCreatedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.escrowId()), new SettlementCandidateCreatedMessage(
                event.eventId(),
                event.orderId(),
                event.escrowId(),
                event.sellerMemberId(),
                event.grossAmount(),
                event.releasedAt(),
                event.confirmationType(),
                event.occurredAt()
        ));
    }
}
