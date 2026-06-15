package com.example.order.infrastructure.kafka.consumer;

import com.example.order.application.service.OrderAuctionCreateService;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.event.AuctionWonEvent;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventConsumer {

    private final OrderAuctionCreateService orderAuctionCreateService;

    @KafkaListener(topics = KafkaTopics.AUCTION_WON, groupId = "order-group", containerFactory = "auctionListenerContainerFactory")
    public void consume(EventEnvelope<AuctionWonEvent> envelope) {
        log.info("auction.won 이벤트 수신. auctionId={}, winnerId={}", envelope.aggregateId(), envelope.recipientId());
        try {
            orderAuctionCreateService.createFromAuctionWon(envelope.aggregateId(), envelope.recipientId(), envelope.payload());
        } catch (Exception e) {
            log.error("auction.won 처리 실패. auctionId={}, winnerId={}, payload={}",
                    envelope.aggregateId(), envelope.recipientId(), envelope.payload(), e);
            throw e;
        }
    }
}
