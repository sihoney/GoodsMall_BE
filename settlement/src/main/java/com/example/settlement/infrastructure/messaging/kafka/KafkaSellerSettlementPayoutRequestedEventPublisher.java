package com.example.settlement.infrastructure.messaging.kafka;

import com.example.settlement.infrastructure.messaging.kafka.contract.SellerSettlementPayoutRequestedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * settlement -> payment 정산 지급 요청 이벤트를 발행하는 Kafka publisher(발행기)다.
 */
@Slf4j
@Component
public class KafkaSellerSettlementPayoutRequestedEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaSellerSettlementPayoutRequestedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${settlement.kafka.topics.settlement-payout-requested:settlement.seller-payout-requested}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    /**
     * settlementId 키로 지급 요청 이벤트를 발행한다.
     * 같은 정산건 이벤트가 동일 파티션에 모이도록 해 순서 추적과 멱등 처리 해석을 돕는다.
     */
    public void publish(SellerSettlementPayoutRequestedMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, String.valueOf(message.settlementId()), json);
        } catch (Exception e) {
            log.error("Failed to serialize SellerSettlementPayoutRequestedMessage. settlementId={}", message.settlementId(), e);
            throw new RuntimeException("Failed to serialize SellerSettlementPayoutRequestedMessage", e);
        }
    }
}

