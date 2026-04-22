package com.todaylunch.auction.infrastructure.messaging.kafka.publisher;

import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.AuctionClosedSoldPayload;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuctionClosedSoldEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(UUID auctionId, UUID sellerId, String auctionTitle, BigDecimal finalPrice) {
        try {
            EventEnvelope<AuctionClosedSoldPayload> envelope = new EventEnvelope<>(
                    UUID.randomUUID(),
                    "AUCTION_CLOSED_SOLD",
                    "auction-service",
                    auctionId,
                    sellerId,
                    Instant.now(),
                    "mock-trace-id",
                    new AuctionClosedSoldPayload(auctionTitle, finalPrice)
            );
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.AUCTION_CLOSED, auctionId.toString(), json);
            log.debug("auction-closed-sold event published: auctionId={}, sellerId={}", auctionId, sellerId);
        } catch (JacksonException e) {
            log.error("auction-closed-sold 이벤트 직렬화 실패: auctionId={}", auctionId, e);
        }
    }
}
