package com.example.payment.infrastructure.messaging.kafka;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaOrderPaymentResultEventPublisher 테스트")
class KafkaOrderPaymentResultEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 결제 결과 이벤트를 지정한 토픽으로 발행한다")
    void publish_sendsEventToKafka() throws Exception {
        KafkaOrderPaymentResultEventPublisher publisher =
                new KafkaOrderPaymentResultEventPublisher(kafkaTemplate, objectMapper);
        UUID orderId = UUID.randomUUID();
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                BigDecimal.valueOf(12_000L),
                OrderPaymentResultStatus.FAILED,
                OrderPaymentFailureReason.WALLET_NOT_FOUND,
                Instant.parse("2024-01-01T12:00:00Z")
        );
        given(objectMapper.writeValueAsString(event)).willReturn("serialized-message");

        publisher.publish(event);

        verify(kafkaTemplate).send(KafkaTopics.ORDER_PAYMENT_RESULT, String.valueOf(orderId), "serialized-message");
    }
}
