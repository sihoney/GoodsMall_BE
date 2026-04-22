package com.todaylunch.auction.infrastructure.messaging.kafka.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.auction.application.port.BidFeeChargeEventPublisher;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 입찰 수수료 차감 요청 이벤트를 Kafka로 발행한다.
 * auctionId를 파티션 키로 사용해 동일 경매 내 이벤트 순서를 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBidFeeChargeEventPublisher implements BidFeeChargeEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(BidFeeChargeRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(KafkaTopics.BID_FEE_CHARGE_REQUESTED, String.valueOf(request.auctionId()), payload);
            log.debug("bid-fee-charge-requested published: auctionId={}, highestBidderId={}",
                    request.auctionId(), request.highestBidderId());
        } catch (JsonProcessingException e) {
            log.error("bid-fee-charge-requested 직렬화 실패: auctionId={}", request.auctionId(), e);
            throw new IllegalStateException("BidFeeChargeRequest 직렬화 실패", e);
        }
    }
}
