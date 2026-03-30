package com.example.payment.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaSellerSettlementPayoutResultEventPublisher 테스트")
class KafkaSellerSettlementPayoutResultEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("정산 지급 결과 이벤트를 settlementId 키로 Kafka에 발행한다")
    void publish_sendsEventToKafka() throws Exception {
        String topic = "payment.seller-payout-result";
        KafkaSellerSettlementPayoutResultEventPublisher publisher =
                new KafkaSellerSettlementPayoutResultEventPublisher(kafkaTemplate, objectMapper, topic);

        UUID settlementId = UUID.randomUUID();
        SellerSettlementPayoutResultMessage message = new SellerSettlementPayoutResultMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                settlementId,
                UUID.randomUUID(),
                9_000L,
                SellerSettlementPayoutResultStatus.SUCCESS,
                null,
                LocalDateTime.of(2026, 4, 1, 3, 10)
        );
        given(objectMapper.writeValueAsString(message)).willReturn("serialized-message");

        publisher.publish(message);

        verify(kafkaTemplate).send(topic, String.valueOf(settlementId), "serialized-message");
    }
}

