package com.example.payment.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.infrastructure.messaging.kafka.contract.SettlementCandidateCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaSettlementCandidateCreatedEventPublisher 테스트")
class KafkaSettlementCandidateCreatedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("정산 원천 이벤트를 escrowId 키로 Kafka에 발행한다")
    void publish_sendsEventToKafka() throws Exception {
        KafkaSettlementCandidateCreatedEventPublisher publisher =
                new KafkaSettlementCandidateCreatedEventPublisher(kafkaTemplate, objectMapper);
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID escrowId = UUID.randomUUID();
        SettlementCandidateCreatedEvent event = new SettlementCandidateCreatedEvent(
                eventId,
                orderId,
                escrowId,
                UUID.randomUUID(),
                BigDecimal.valueOf(10_000L),
                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                ConfirmationType.AUTO,
                LocalDateTime.of(2024, 1, 1, 12, 0, 1)
        );
        given(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any(SettlementCandidateCreatedMessage.class)))
                .willReturn("serialized-message");

        publisher.publish(event);

        ArgumentCaptor<SettlementCandidateCreatedMessage> captor = ArgumentCaptor.forClass(SettlementCandidateCreatedMessage.class);
        verify(objectMapper).writeValueAsString(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo(event.eventId());
        assertThat(captor.getValue().orderId()).isEqualTo(event.orderId());
        assertThat(captor.getValue().escrowId()).isEqualTo(event.escrowId());
        assertThat(captor.getValue().sellerMemberId()).isEqualTo(event.sellerMemberId());
        assertThat(captor.getValue().grossAmount()).isEqualTo(event.grossAmount());
        assertThat(captor.getValue().releasedAt()).isEqualTo(Instant.parse("2024-01-01T03:00:00Z"));
        assertThat(captor.getValue().confirmationType()).isEqualTo(event.confirmationType());
        assertThat(captor.getValue().occurredAt()).isEqualTo(Instant.parse("2024-01-01T03:00:01Z"));
        verify(kafkaTemplate).send(KafkaTopics.SETTLEMENT_CANDIDATE_CREATED, String.valueOf(escrowId), "serialized-message");
    }
}
