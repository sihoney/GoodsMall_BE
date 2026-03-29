package com.example.payment.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaSettlementCandidateCreatedEventPublisher 테스트")
class KafkaSettlementCandidateCreatedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("정산 원천 이벤트를 escrowId 키로 Kafka에 발행한다")
    void publish_sendsEventToKafka() {
        String topic = "payment.settlement-candidate-created";
        KafkaSettlementCandidateCreatedEventPublisher publisher =
                new KafkaSettlementCandidateCreatedEventPublisher(kafkaTemplate, topic);
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID escrowId = UUID.randomUUID();
        SettlementCandidateCreatedEvent event = new SettlementCandidateCreatedEvent(
                eventId,
                orderId,
                escrowId,
                UUID.randomUUID(),
                10_000L,
                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                ConfirmationType.AUTO,
                LocalDateTime.of(2024, 1, 1, 12, 0, 1)
        );

        publisher.publish(event);

        verify(kafkaTemplate).send(topic, String.valueOf(escrowId), new SettlementCandidateCreatedMessage(
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
