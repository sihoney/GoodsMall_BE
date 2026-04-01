package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaAutoPurchaseConfirmedEventPublisher 테스트")
class KafkaAutoPurchaseConfirmedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("자동 구매확정 이벤트를 지정 토픽으로 발행한다")
    void publish_sendsEventToKafka() throws Exception {
        String topic = "payment.auto-purchase-confirmed";
        KafkaAutoPurchaseConfirmedEventPublisher publisher =
                new KafkaAutoPurchaseConfirmedEventPublisher(kafkaTemplate, objectMapper, topic);
        UUID orderId = UUID.randomUUID();
        AutoPurchaseConfirmedEvent event = new AutoPurchaseConfirmedEvent(
                orderId,
                UUID.randomUUID(),
                LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        );
        given(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any(AutoPurchaseConfirmedMessage.class)))
                .willReturn("serialized-message");

        publisher.publish(event);

        ArgumentCaptor<AutoPurchaseConfirmedMessage> captor = ArgumentCaptor.forClass(AutoPurchaseConfirmedMessage.class);
        verify(objectMapper).writeValueAsString(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(event.orderId());
        assertThat(captor.getValue().buyerMemberId()).isEqualTo(event.buyerMemberId());
        assertThat(captor.getValue().confirmedAt()).isEqualTo(Instant.parse("2024-01-01T03:00:00Z"));
        verify(kafkaTemplate).send(topic, String.valueOf(orderId), "serialized-message");
    }
}
