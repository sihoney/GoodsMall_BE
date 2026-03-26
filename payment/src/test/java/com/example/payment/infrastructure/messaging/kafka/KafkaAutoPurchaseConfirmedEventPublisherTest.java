package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.infrastructure.messaging.kafka.contract.AutoPurchaseConfirmedMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaAutoPurchaseConfirmedEventPublisher 테스트")
class KafkaAutoPurchaseConfirmedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("자동 구매확정 이벤트를 지정 토픽으로 발행한다")
    void publish_sendsEventToKafka() {
        String topic = "payment.auto-purchase-confirmed";
        KafkaAutoPurchaseConfirmedEventPublisher publisher =
                new KafkaAutoPurchaseConfirmedEventPublisher(kafkaTemplate, topic);
        UUID orderId = UUID.randomUUID();
        AutoPurchaseConfirmedEvent event = new AutoPurchaseConfirmedEvent(
                orderId,
                UUID.randomUUID(),
                LocalDateTime.of(2024, 1, 1, 12, 0, 0)
        );

        publisher.publish(event);

        verify(kafkaTemplate).send(topic, String.valueOf(orderId), new AutoPurchaseConfirmedMessage(
                event.orderId(),
                event.buyerMemberId(),
                event.confirmedAt()
        ));
    }
}
