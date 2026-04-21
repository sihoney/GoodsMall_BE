package com.todaylunch.auction.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.auction.application.port.BidFeeChargeEventPublisher;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 입찰 수수료 차감 요청 이벤트를 Kafka로 발행한다.
 * auctionId를 파티션 키로 사용해 동일 경매 내 이벤트 순서를 보장한다.
 */
@Slf4j
@Component
public class KafkaBidFeeChargeEventPublisher implements BidFeeChargeEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaBidFeeChargeEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${auction.kafka.topic.bid-fee-charge-requested:auction.bid-fee.charge-requested}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(BidFeeChargeRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(topic, String.valueOf(request.auctionId()), payload);
            log.debug("bid-fee-charge-requested published: auctionId={}, highestBidderId={}",
                    request.auctionId(), request.highestBidderId());
        } catch (JsonProcessingException e) {
            log.error("bid-fee-charge-requested 직렬화 실패: auctionId={}", request.auctionId(), e);
            throw new IllegalStateException("BidFeeChargeRequest 직렬화 실패", e);
        }
    }
}
