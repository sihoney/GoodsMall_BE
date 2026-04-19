package com.example.settlement.infrastructure.messaging.kafka;

import static org.mockito.Mockito.verify;

import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

import com.example.settlement.infrastructure.messaging.kafka.contract.SettlementPayoutType;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaSellerSettlementPayoutRequestedEventPublisher 테스트")
class KafkaSellerSettlementPayoutRequestedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("정산 지급 요청 이벤트를 settlementId 키로 Kafka에 발행한다")
    void publish_sendsEventToKafka() throws Exception {
        String topic = "settlement.seller-payout-requested";
        KafkaSellerSettlementPayoutRequestedEventPublisher publisher =
                new KafkaSellerSettlementPayoutRequestedEventPublisher(kafkaTemplate, objectMapper, topic);

        UUID settlementId = UUID.randomUUID();
        SellerSettlementPayoutRequestedMessage message = new SellerSettlementPayoutRequestedMessage(
                UUID.randomUUID(),
                settlementId,
                SettlementPayoutType.MONTHLY,
                UUID.randomUUID(),
                2026,
                3,
                9_000L,
                LocalDateTime.of(2026, 4, 1, 3, 5)
        );
        given(objectMapper.writeValueAsString(message)).willReturn("serialized-message");

        publisher.publish(message);

        verify(kafkaTemplate).send(topic, String.valueOf(settlementId), "serialized-message");
    }
}

