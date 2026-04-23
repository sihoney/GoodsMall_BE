package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.infrastructure.messaging.kafka.contract.SellerSettlementPayoutResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * payment -> settlement 정산 지급 결과 이벤트를 발행하는 Kafka publisher(발행기)다.
 */
@Slf4j
@Component
public class KafkaSellerSettlementPayoutResultEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaSellerSettlementPayoutResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * settlementId 키로 지급 결과 이벤트를 발행한다.
     * 요청 이벤트와 동일한 정산건 기준으로 결과를 추적하기 쉽도록 settlementId를 key로 고정한다.
     */
    public void publish(SellerSettlementPayoutResultMessage event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaTopics.SETTLEMENT_PAYOUT_RESULT, String.valueOf(event.settlementId()), message);
        } catch (Exception e) {
            log.error("Failed to serialize SellerSettlementPayoutResultMessage. settlementId={}", event.settlementId(), e);
            throw new RuntimeException("Failed to serialize SellerSettlementPayoutResultMessage", e);
        }
    }
}

