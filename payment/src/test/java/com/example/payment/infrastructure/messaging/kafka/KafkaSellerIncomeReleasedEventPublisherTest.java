package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SellerIncomeReleasedEvent;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerIncomeReleasedMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaSellerIncomeReleasedEventPublisher 테스트")
class KafkaSellerIncomeReleasedEventPublisherTest {

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("판매자 입금 완료 이벤트를 지정 토픽으로 발행한다")
    void publish_sendsEventToKafka() {
        String topic = "payment.seller-income-released";
        KafkaSellerIncomeReleasedEventPublisher publisher =
                new KafkaSellerIncomeReleasedEventPublisher(kafkaTemplate, topic);
        UUID orderId = UUID.randomUUID();
        SellerIncomeReleasedEvent event = new SellerIncomeReleasedEvent(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                10_000L,
                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                ConfirmationType.AUTO
        );

        publisher.publish(event);

        verify(kafkaTemplate).send(topic, String.valueOf(orderId), new SellerIncomeReleasedMessage(
                event.orderId(),
                event.sellerMemberId(),
                event.sellerWalletId(),
                event.releasedAmount(),
                event.releasedAt(),
                event.confirmationType()
        ));
    }
}
