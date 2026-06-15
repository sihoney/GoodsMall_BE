package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.KafkaTopics;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.OrderConfirmedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedConsumer {

    private final AuctionRepository auctionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED)
    public void handle(String payload) throws Exception {
        EventEnvelope<OrderConfirmedMessage> envelope =
                objectMapper.readValue(payload, new TypeReference<>() {});
        OrderConfirmedMessage message = envelope.payload();

        if (message.auctionId() == null) {
            return;
        }

        Auction auction = auctionRepository.findById(message.auctionId());

        if (auction.getStatus() != AuctionStatus.PENDING_PAYMENT) {
            log.warn("중복 이벤트 또는 잘못된 상태 — 무시: auctionId={}, status={}",
                    auction.getAuctionId(), auction.getStatus());
            return;
        }

        auction.complete();
        auctionRepository.save(auction);

        log.info("경매 낙찰 확정: auctionId={}", auction.getAuctionId());
    }
}
