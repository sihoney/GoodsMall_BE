package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentFailureReason;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.OrderPaymentResultStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaOrderPaymentResultEventPublisher 테스트")
class KafkaOrderPaymentResultEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("주문 결제 결과 이벤트를 지정한 토픽으로 발행한다")
    void publish_sendsEventToKafka() {
        String topic = "payment.order-payment-result";
        KafkaOrderPaymentResultEventPublisher publisher =
                new KafkaOrderPaymentResultEventPublisher(kafkaTemplate, topic);
        UUID orderId = UUID.randomUUID();
        OrderPaymentResultMessage event = new OrderPaymentResultMessage(
                "evt-1",
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderPaymentResultStatus.FAILED,
                12_000L,
                10_000L,
                null,
                null,
                OrderPaymentFailureReason.WALLET_NOT_FOUND,
                "Wallet not found.",
                LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        );

        publisher.publish(event);

        verify(kafkaTemplate).send(topic, String.valueOf(orderId), event);
    }
}
