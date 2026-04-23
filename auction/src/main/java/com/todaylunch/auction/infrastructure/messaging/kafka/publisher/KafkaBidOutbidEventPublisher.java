package com.todaylunch.auction.infrastructure.messaging.kafka.publisher;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBidOutbidEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(UUID auctionId, UUID outbidBidderId) {
        try {
            EventEnvelope<Void> envelope = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "AUCTION_BID_OUTBID",
                    "auction-service",
                    auctionId,
                    outbidBidderId,
                    Instant.now(),
                    "mock-trace-id",
                    null
            );
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.BID_OUTBID, auctionId.toString(), json);
            log.debug("bid-outbid event published: auctionId={}, outbidBidderId={}", auctionId, outbidBidderId);
        } catch (JacksonException e) {
            log.error("bid-outbid 이벤트 직렬화 실패: auctionId={}", auctionId, e);
        }
    }
}
