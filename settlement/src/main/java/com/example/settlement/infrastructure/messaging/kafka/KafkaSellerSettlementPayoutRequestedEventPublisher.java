package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * settlement -> payment 정산 지급 요청 이벤트를 발행하는 Kafka publisher(발행기)다.
 */
@Component
public class KafkaSellerSettlementPayoutRequestedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaSellerSettlementPayoutRequestedEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${settlement.kafka.topics.settlement-payout-requested:settlement.seller-payout-requested}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * settlementId 키로 지급 요청 이벤트를 발행한다.
     */
    public void publish(SellerSettlementPayoutRequestedMessage message) {
        kafkaTemplate.send(topic, String.valueOf(message.settlementId()), message);
    }
}

