package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.event.SellerIncomeReleasedEvent;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import com.example.payment.infrastructure.messaging.kafka.contract.SellerIncomeReleasedMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
/**
 * 판매자 정산 완료 내부 이벤트를 Kafka 계약 메시지로 변환해 발행하는 adapter다.
 */
public class KafkaSellerIncomeReleasedEventPublisher implements SellerIncomeReleasedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaSellerIncomeReleasedEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.seller-income-released:payment.seller-income-released}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    /**
     * 내부 정산 이벤트를 seller income released 메시지로 바꿔 orderId key로 발행한다.
     */
    public void publish(SellerIncomeReleasedEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.orderId()), new SellerIncomeReleasedMessage(
                event.orderId(),
                event.sellerMemberId(),
                event.sellerWalletId(),
                event.releasedAmount(),
                event.releasedAt(),
                event.confirmationType()
        ));
    }
}
