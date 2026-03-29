package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * payment -> settlement 정산 지급 결과 이벤트를 발행하는 Kafka publisher(발행기)다.
 */
@Component
public class KafkaSellerSettlementPayoutResultEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaSellerSettlementPayoutResultEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${payment.kafka.topics.settlement-payout-result:payment.seller-payout-result}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * settlementId 키로 지급 결과 이벤트를 발행한다.
     * 요청 이벤트와 동일한 정산건 기준으로 결과를 추적하기 쉽도록 settlementId를 key로 고정한다.
     */
    public void publish(SellerSettlementPayoutResultMessage event) {
        kafkaTemplate.send(topic, String.valueOf(event.settlementId()), event);
    }
}

