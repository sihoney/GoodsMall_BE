package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * payment -> auction 경매 입찰 보증금 처리 결과 이벤트를 발행하는 Kafka publisher다.
 */
@Slf4j
@Component
public class KafkaAuctionBidFeeChargeResultEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaAuctionBidFeeChargeResultEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSuccess(BidFeeChargeSucceededMessage event) {
        publish(KafkaTopics.AUCTION_BID_FEE_CHARGE_SUCCEEDED, String.valueOf(event.auctionId()), event);
    }

    public void publishFailure(BidFeeChargeFailedMessage event) {
        publish(KafkaTopics.AUCTION_BID_FEE_CHARGE_FAILED, String.valueOf(event.auctionId()), event);
    }

    private void publish(String topic, String key, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            log.error("경매 입찰 보증금 처리 결과 이벤트 직렬화에 실패했습니다. topic={} key={}", topic, key, e);
            throw new RuntimeException("경매 입찰 보증금 처리 결과 이벤트 직렬화에 실패했습니다.", e);
        }
    }
}
