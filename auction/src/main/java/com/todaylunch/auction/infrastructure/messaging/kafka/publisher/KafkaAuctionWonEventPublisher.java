package com.todaylunch.auction.infrastructure.messaging.kafka.publisher;

import com.todaylunch.auction.infrastructure.messaging.kafka.AuctionEventTypes;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.AuctionWonPayload;
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
public class KafkaAuctionWonEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(UUID auctionId, UUID winnerId, String auctionTitle, BigDecimal finalPrice, UUID productId, BigDecimal orderPrice) {
        try {
            EventEnvelope<AuctionWonPayload> envelope = new EventEnvelope<>(
                    UUID.randomUUID(),
                    AuctionEventTypes.AUCTION_WON,
                    "auction-service",
                    auctionId,
                    winnerId,
                    Instant.now(),
                    "mock-trace-id",
                    new AuctionWonPayload(auctionTitle, finalPrice, productId, orderPrice)
            );
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(KafkaTopics.AUCTION_WON, auctionId.toString(), json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("auction-won 이벤트 발행 실패: auctionId={}, winnerId={}", auctionId, winnerId, ex);
                        } else {
                            log.debug("auction-won 이벤트 발행 성공: auctionId={}, winnerId={}", auctionId, winnerId);
                        }
                    });
        } catch (JacksonException e) {
            log.error("auction-won 이벤트 직렬화 실패: auctionId={}", auctionId, e);
        }
    }
}
